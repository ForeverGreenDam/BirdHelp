package com.greendam.birdhelp.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.utils.AliOssUtil;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.PreviewPage;
import com.greendam.birdhelp.model.vo.PreviewVO;
import com.greendam.birdhelp.service.FileStorageService;
import com.greendam.birdhelp.service.PreviewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 文件预览服务实现类。
 * </p>
 *
 * <h3>渲染管道</h3>
 * <pre>
 * 原始文件 → LibreOffice 无头转 PDF → PDFBox 逐页渲染 96 DPI JPEG(85%质量) → 上传 OSS（获 URL）
 *   → 写入 file_record.preview_pages → Redis 热缓存 1h
 * </pre>
 *
 * <h3>三级缓存</h3>
 * <ol>
 *   <li>Redis 热缓存（{@code preview:{fileId}}，TTL 1h）→ fileHash 一致 → 直接返回</li>
 *   <li>MySQL {@code file_record.preview_pages} → fileHash 一致 → 返回 + 回填 Redis</li>
 *   <li>都不命中 → 重新渲染 → 更新 MySQL + Redis</li>
 * </ol>
 *
 * <h3>环境要求</h3>
 * <p>需要系统安装 LibreOffice 并加入 PATH。Docker 环境已内置。
 * 若 LibreOffice 不可用，仅支持 PDF 源文件的直接渲染（跳过转换步骤）。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class PreviewServiceImpl implements PreviewService {

    private static final String REDIS_PREVIEW_PREFIX = "preview:";
    private static final long REDIS_PREVIEW_TTL_HOURS = 1;
    private static final int RENDER_DPI = 96;
    private static final float JPEG_QUALITY = 0.85f;
    private static final int LIBREOFFICE_TIMEOUT_SECONDS = 60;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private FileRecordMapper fileRecordMapper;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private AliOssUtil aliOssUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PreviewVO getPreview(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        // 计算文件哈希用于缓存校验
        byte[] fileBytes = fileStorageService.load(record.getFileUrl());
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件内容为空或无法读取");
        }
        String fileHash = DigestUtil.md5Hex(fileBytes);

        // 第一级：Redis 热缓存
        String redisKey = REDIS_PREVIEW_PREFIX + fileId;
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            try {
                PreviewVO cachedVO = objectMapper.readValue(cached, PreviewVO.class);
                if (fileHash.equals(cachedVO.getFileHash())) {
                    log.info("预览命中 Redis 缓存: fileId={}", fileId);
                    return cachedVO;
                }
            } catch (JsonProcessingException e) {
                log.warn("Redis 预览缓存解析失败，将重新渲染: fileId={}", fileId, e);
            }
        }

        // 第二级：MySQL preview_pages 持久化缓存
        String previewPagesJson = record.getPreviewPages();
        if (previewPagesJson != null && !previewPagesJson.isEmpty()) {
            try {
                Map<String, Object> cachedData = objectMapper.readValue(previewPagesJson,
                        new TypeReference<Map<String, Object>>() {
                        });
                String cachedHash = (String) cachedData.get("fileHash");
                if (fileHash.equals(cachedHash)) {
                    PreviewVO vo = buildPreviewFromCache(fileId, fileHash, cachedData);
                    // 回填 Redis
                    cacheToRedis(redisKey, vo);
                    log.info("预览命中 MySQL 缓存: fileId={}", fileId);
                    return vo;
                }
            } catch (JsonProcessingException e) {
                log.warn("MySQL 预览缓存解析失败，将重新渲染: fileId={}", fileId, e);
            }
        }

        // 第三级：重新渲染
        log.info("缓存未命中，开始渲染预览: fileId={}, fileType={}", fileId, record.getFileType());
        PreviewVO vo = renderPreview(record, fileBytes, fileHash);

        // 持久化到 MySQL
        persistPreviewPages(record, vo);

        // 写入 Redis 热缓存
        cacheToRedis(redisKey, vo);

        return vo;
    }

    @Override
    public PreviewVO refreshPreview(Long fileId) {
        // 清除 Redis 缓存
        stringRedisTemplate.delete(REDIS_PREVIEW_PREFIX + fileId);
        // 清除 MySQL 缓存
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record != null) {
            record.setPreviewPages(null);
            fileRecordMapper.updateById(record);
        }
        log.info("预览缓存已清除，将强制重新渲染: fileId={}", fileId);
        return getPreview(fileId);
    }

    // ==================== 内部渲染方法 ====================

    /**
     * 执行完整渲染管道：源文件 → PDF → 逐页 PNG → 上传 OSS。
     */
    private PreviewVO renderPreview(FileRecord record, byte[] fileBytes, String fileHash) {
        Path tempDir = null;
        try {
            // 创建临时工作目录
            tempDir = Files.createTempDirectory("birdhelp_preview_");
            String ext = extractExt(record.getFileName()).toLowerCase();

            // 第一步：获取 PDF 文件
            Path pdfPath;
            if ("pdf".equals(ext)) {
                // 已是 PDF，直接写入临时文件
                pdfPath = tempDir.resolve("source.pdf");
                Files.write(pdfPath, fileBytes);
            } else {
                // 非 PDF 格式，需要 LibreOffice 转换
                Path sourcePath = tempDir.resolve("source." + ext);
                Files.write(sourcePath, fileBytes);
                pdfPath = convertToPdf(sourcePath, tempDir);
            }

            // 第二步：PDFBox 逐页渲染 PNG
            List<PreviewPage> pages = renderPdfPages(pdfPath, record, tempDir);

            PreviewVO vo = PreviewVO.builder()
                    .fileId(record.getId())
                    .fileHash(fileHash)
                    .totalPages(pages.size())
                    .pages(pages)
                    .build();

            log.info("预览渲染完成: fileId={}, totalPages={}", record.getId(), pages.size());
            return vo;

        } catch (Exception e) {
            log.error("预览渲染失败: fileId={}", record.getId(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "预览渲染失败: " + e.getMessage());
        } finally {
            // 清理临时目录
            if (tempDir != null) {
                try {
                    FileUtil.del(tempDir.toFile());
                } catch (Exception e) {
                    log.warn("清理临时目录失败: {}", tempDir, e);
                }
            }
        }
    }

    /**
     * 使用 LibreOffice 无头模式将办公文档转为 PDF。
     */
    private Path convertToPdf(Path sourcePath, Path outputDir) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputDir.toAbsolutePath().toString(),
                sourcePath.toAbsolutePath().toString()
        );

        log.info("执行 LibreOffice 转换: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        boolean finished = process.waitFor(LIBREOFFICE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("LibreOffice 转换超时（" + LIBREOFFICE_TIMEOUT_SECONDS + "s）");
        }

        if (process.exitValue() != 0) {
            String errorOutput = new String(process.getInputStream().readAllBytes());
            throw new IOException("LibreOffice 转换失败 (exit=" + process.exitValue() + "): " + errorOutput);
        }

        // 查找生成的 PDF 文件
        String sourceName = sourcePath.getFileName().toString();
        String baseName = sourceName.substring(0, sourceName.lastIndexOf('.'));
        Path pdfPath = outputDir.resolve(baseName + ".pdf");

        if (!Files.exists(pdfPath)) {
            throw new IOException("LibreOffice 转换后未找到 PDF 文件: " + pdfPath);
        }

        log.info("LibreOffice 转换完成: {} → {}", sourcePath.getFileName(), pdfPath.getFileName());
        return pdfPath;
    }

    /**
     * 使用 PDFBox 将 PDF 逐页渲染为 150 DPI PNG 图片并上传 OSS。
     */
    private List<PreviewPage> renderPdfPages(Path pdfPath, FileRecord record, Path tempDir) throws IOException {
        List<PreviewPage> pages = new ArrayList<>();

        // 解析 outline 获取每页布局标注
        Map<Integer, Map<String, String>> pageAnnotations = parseOutlineAnnotations(record.getOutline());

        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                int pageNumber = i + 1;

                // 渲染为 BufferedImage（96 DPI，RGB 色彩模式，屏幕预览足够）
                BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);

                // 编码为 JPEG（85% 质量，比 PNG 小 10-20 倍，预览无感知差异）
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    jpegWriter.setOutput(ios);
                    ImageWriteParam param = jpegWriter.getDefaultWriteParam();
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(JPEG_QUALITY);
                    jpegWriter.write(null, new IIOImage(image, null, null), param);
                }
                jpegWriter.dispose();
                byte[] jpgBytes = baos.toByteArray();

                // 上传到 OSS
                String objectName = buildPreviewObjectName(record, pageNumber);
                String imageUrl = aliOssUtil.upload(jpgBytes, objectName);

                // 获取该页的布局标注
                Map<String, String> annotations = pageAnnotations.getOrDefault(pageNumber, Collections.emptyMap());

                PreviewPage page = PreviewPage.builder()
                        .pageNumber(pageNumber)
                        .imageUrl(imageUrl)
                        .layoutType(annotations.get("layoutType"))
                        .title(annotations.get("title"))
                        .build();

                pages.add(page);
                log.info("页面已渲染: fileId={}, page={}/{}, imageUrl={}", record.getId(), pageNumber, totalPages, imageUrl);
            }
        }

        return pages;
    }

    // ==================== 辅助方法 ====================

    /**
     * 从 outline JSON 中解析每页的 layout_type 和 title，用于预览布局标注。
     *
     * <p>PPT 大纲格式：{@code {"slides": [{"page_number": 1, "title": "...", "layout_type": "..."}]}}
     * Word/PDF 大纲格式：{@code {"sections": [{"section_number": 1, "heading": "..."}]}</p>
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, Map<String, String>> parseOutlineAnnotations(String outlineJson) {
        Map<Integer, Map<String, String>> result = new HashMap<>();
        if (outlineJson == null || outlineJson.isEmpty()) {
            return result;
        }

        try {
            Map<String, Object> outline = objectMapper.readValue(outlineJson,
                    new TypeReference<Map<String, Object>>() {
                    });

            // 尝试 PPT 格式（slides）
            if (outline.containsKey("slides")) {
                List<Map<String, Object>> slides = (List<Map<String, Object>>) outline.get("slides");
                for (Map<String, Object> slide : slides) {
                    int pageNumber = toInt(slide.get("page_number"));
                    Map<String, String> annotations = new HashMap<>();
                    if (slide.get("title") != null) {
                        annotations.put("title", slide.get("title").toString());
                    }
                    if (slide.get("layout_type") != null) {
                        annotations.put("layoutType", slide.get("layout_type").toString());
                    }
                    result.put(pageNumber, annotations);
                }
            }

            // 尝试 Word/PDF 格式（sections）
            if (outline.containsKey("sections")) {
                List<Map<String, Object>> sections = (List<Map<String, Object>>) outline.get("sections");
                for (Map<String, Object> section : sections) {
                    int sectionNumber = toInt(section.get("section_number"));
                    Map<String, String> annotations = new HashMap<>();
                    if (section.get("heading") != null) {
                        annotations.put("title", section.get("heading").toString());
                    }
                    result.put(sectionNumber, annotations);
                }
            }
        } catch (Exception e) {
            log.warn("解析 outline 布局标注失败: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 构建预览图片的 OSS 对象名。
     */
    private String buildPreviewObjectName(FileRecord record, int pageNumber) {
        return String.format("preview/%d/%s/page_%03d.jpg",
                record.getProjectId(),
                record.getId(),
                pageNumber);
    }

    /**
     * 从缓存的 JSON 数据重建 PreviewVO。
     */
    @SuppressWarnings("unchecked")
    private PreviewVO buildPreviewFromCache(Long fileId, String fileHash, Map<String, Object> cachedData) {
        List<Map<String, Object>> pagesData = (List<Map<String, Object>>) cachedData.get("pages");
        List<PreviewPage> pages = new ArrayList<>();

        if (pagesData != null) {
            for (Map<String, Object> pageData : pagesData) {
                PreviewPage page = PreviewPage.builder()
                        .pageNumber(toInt(pageData.get("pageNumber")))
                        .imageUrl((String) pageData.get("imageUrl"))
                        .layoutType((String) pageData.get("layoutType"))
                        .title((String) pageData.get("title"))
                        .build();
                pages.add(page);
            }
        }

        return PreviewVO.builder()
                .fileId(fileId)
                .fileHash(fileHash)
                .totalPages(pages.size())
                .pages(pages)
                .build();
    }

    /**
     * 将预览结果持久化到 file_record.preview_pages（JSON 格式）。
     */
    private void persistPreviewPages(FileRecord record, PreviewVO vo) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("fileHash", vo.getFileHash());
            data.put("pages", vo.getPages());
            String json = objectMapper.writeValueAsString(data);
            record.setPreviewPages(json);
            fileRecordMapper.updateById(record);
            log.info("预览页面已持久化: fileId={}, jsonLength={}", record.getId(), json.length());
        } catch (JsonProcessingException e) {
            log.error("序列化预览数据失败: fileId={}", record.getId(), e);
        }
    }

    /**
     * 将预览结果写入 Redis 热缓存。
     */
    private void cacheToRedis(String key, PreviewVO vo) {
        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(key, json, REDIS_PREVIEW_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.warn("Redis 预览缓存写入失败", e);
        }
    }

    private String extractExt(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx < 0 ? "" : fileName.substring(dotIdx + 1);
    }

    private int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
