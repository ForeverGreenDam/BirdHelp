package com.greendam.birdhelp.common.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * 可重复读取 Body 并支持 multipart 的请求包装器。
 * </p>
 *
 * <p>将请求体缓存到内存中，使 Filter 读取 Body 后 Controller 仍能再次读取。
 * 重写 {@code getParts()} / {@code getPart()} 从缓存的原始字节中手动解析 multipart parts，
 * 解决 SignFilter 消费 InputStream 后 Spring StandardMultipartResolver 无法再解析的问题。</p>
 *
 * @author ForeverGreenDam
 */
public class CachedBodyHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private Collection<Part> cachedParts;

    public CachedBodyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
    /**
     * form field 参数缓存（name → value），与 parts 一起在首次 getParts() 时填充
     */
    private Map<String, String[]> cachedParams;

    /**
     * 从 parsed parts 中提取 form field（非文件的 part）构建参数表
     */
    private static Map<String, String[]> buildParamMap(Collection<Part> parts) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Part part : parts) {
            if (part.getSubmittedFileName() != null) {
                continue; // file part，不作为普通参数
            }
            try {
                String value = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                map.computeIfAbsent(part.getName(), k -> new ArrayList<>()).add(value);
            } catch (IOException ignored) {
                // should not happen for ByteArrayInputStream
            }
        }
        Map<String, String[]> result = new LinkedHashMap<>();
        for (var entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        return result;
    }

    // ==================== multipart 支持 ====================

    private static int indexOf(byte[] data, byte[] pattern, int fromIndex) {
        outer:
        for (int i = fromIndex; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static boolean startsWith(byte[] data, byte[] prefix, int offset) {
        if (offset + prefix.length > data.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[offset + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String extractBoundary(String contentType) {
        for (String seg : contentType.split(";")) {
            seg = seg.trim();
            if (seg.startsWith("boundary=")) {
                return seg.substring("boundary=".length());
            }
        }
        return null;
    }

    private static Map<String, List<String>> parsePartHeaders(String headerBlock) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String line : headerBlock.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim().toLowerCase(Locale.ENGLISH);
                String value = line.substring(colon + 1).trim();
                result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        return result;
    }

    private static Part buildPart(Map<String, List<String>> headers, byte[] content) {
        String disp = getHeaderValue(headers, "content-disposition");
        if (disp == null) {
            return null;
        }
        String name = extractDispositionParam(disp, "name");
        if (name == null) {
            return null;
        }
        String filename = extractDispositionParam(disp, "filename");
        String partContentType = getHeaderValue(headers, "content-type");
        return new CachedPart(name, filename, partContentType, content, headers);
    }

    private static String extractDispositionParam(String disp, String paramName) {
        for (String seg : disp.split(";")) {
            seg = seg.trim();
            if (seg.startsWith(paramName + "=")) {
                return unquote(seg.substring(paramName.length() + 1));
            }
        }
        return null;
    }

    private static String unquote(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String getHeaderValue(Map<String, List<String>> headers, String name) {
        List<String> values = headers.get(name.toLowerCase(Locale.ENGLISH));
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    /** 以 UTF-8 字符串返回缓存的请求体 */
    public String getBodyAsString() {
        return new String(cachedBody, StandardCharsets.UTF_8);
    }

    // ---- helper methods ----

    /**
     * 返回缓存的原始字节（用于签名，避免 UTF-8 编解码差异）
     */
    public byte[] getCachedBody() {
        return cachedBody;
    }

    @Override
    public Collection<Part> getParts() throws IOException {
        if (cachedParts == null) {
            cachedParts = parseParts();
            cachedParams = buildParamMap(cachedParts);
        }
        return cachedParts;
    }

    @Override
    public Part getPart(String name) throws IOException {
        for (Part part : getParts()) {
            if (name.equals(part.getName())) {
                return part;
            }
        }
        return null;
    }

    @Override
    public String getParameter(String name) {
        if (cachedParams != null) {
            String[] values = cachedParams.get(name);
            if (values != null && values.length > 0) {
                return values[0];
            }
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (cachedParams != null) {
            Map<String, String[]> merged = new LinkedHashMap<>(super.getParameterMap());
            merged.putAll(cachedParams);
            return Collections.unmodifiableMap(merged);
        }
        return super.getParameterMap();
    }

    @Override
    public String[] getParameterValues(String name) {
        if (cachedParams != null) {
            String[] values = cachedParams.get(name);
            if (values != null) {
                return values;
            }
        }
        return super.getParameterValues(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (cachedParams != null) {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getParameterNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            names.addAll(cachedParams.keySet());
            return Collections.enumeration(names);
        }
        return super.getParameterNames();
    }

    /**
     * 从缓存的原始字节中手动解析 multipart parts。
     *
     * <p>multipart body 标准格式：</p>
     * <pre>
     * --BOUNDARY\r\n
     * header1: value1\r\n
     * header2: value2\r\n
     * \r\n
     * content bytes\r\n
     * --BOUNDARY\r\n
     * ...
     * --BOUNDARY--\r\n
     * </pre>
     */
    private Collection<Part> parseParts() throws IOException {
        String contentType = getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/form-data")) {
            return Collections.emptyList();
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return Collections.emptyList();
        }

        List<Part> parts = new ArrayList<>();
        byte[] delim = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] headerEnd = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        int pos = 0;
        // 跳过第一个 boundary 之前的无关内容（preamble）
        pos = indexOf(cachedBody, delim, pos);
        if (pos < 0) {
            return parts;
        }
        pos += delim.length;
        // 跳过 boundary 行尾的 \r\n
        if (startsWith(cachedBody, crlf, pos)) {
            pos += crlf.length;
        }

        while (pos < cachedBody.length) {
            // 检查是否到达结束 boundary (--boundary--)
            if (startsWith(cachedBody, "--".getBytes(StandardCharsets.UTF_8), pos)) {
                break;
            }

            // 查找 header 结束位置 (\r\n\r\n)
            int headersEnd = indexOf(cachedBody, headerEnd, pos);
            if (headersEnd < 0) {
                break;
            }

            // 解析 headers
            String headerBlock = new String(cachedBody, pos, headersEnd - pos, StandardCharsets.UTF_8);
            Map<String, List<String>> headers = parsePartHeaders(headerBlock);

            // content 起始位置
            int contentStart = headersEnd + headerEnd.length;

            // 查找下一个 boundary 作为 content 结束
            int nextDelim = indexOf(cachedBody, delim, contentStart);
            if (nextDelim < 0) {
                // 没有更多 boundary，取到末尾
                byte[] content = Arrays.copyOfRange(cachedBody, contentStart, cachedBody.length);
                Part part = buildPart(headers, content);
                if (part != null) {
                    parts.add(part);
                }
                break;
            }

            // content 结束于下一个 boundary 前一个 \r\n
            int contentEnd = nextDelim;
            if (contentEnd >= 2
                    && cachedBody[contentEnd - 2] == '\r'
                    && cachedBody[contentEnd - 1] == '\n') {
                contentEnd -= 2;
            }

            byte[] content = Arrays.copyOfRange(cachedBody, contentStart, Math.max(contentStart, contentEnd));
            Part part = buildPart(headers, content);
            if (part != null) {
                parts.add(part);
            }

            // 跳过当前 boundary
            pos = nextDelim + delim.length;
            // 跳过 boundary 行尾的 \r\n
            if (startsWith(cachedBody, crlf, pos)) {
                pos += crlf.length;
            }
        }

        return parts;
    }

    // ==================== Part 适配实现 ====================

    private static class CachedPart implements Part {
        private final String name;
        private final String submittedFileName;
        private final String contentType;
        private final byte[] content;
        private final Map<String, List<String>> headers;

        CachedPart(String name, String submittedFileName, String contentType,
                   byte[] content, Map<String, List<String>> headers) {
            this.name = name;
            this.submittedFileName = submittedFileName;
            this.contentType = contentType;
            this.content = content;
            this.headers = headers;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSubmittedFileName() {
            return submittedFileName;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public void write(String fileName) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(content);
            }
        }

        @Override
        public void delete() {
            // no-op
        }

        @Override
        public String getHeader(String name) {
            List<String> values = headers.get(name.toLowerCase(Locale.ENGLISH));
            return (values != null && !values.isEmpty()) ? values.get(0) : null;
        }

        @Override
        public Collection<String> getHeaders(String name) {
            List<String> values = headers.get(name.toLowerCase(Locale.ENGLISH));
            return values != null ? values : Collections.emptyList();
        }

        @Override
        public Collection<String> getHeaderNames() {
            return headers.keySet();
        }
    }
}
