package com.greendam.birdhelp.service.impl;

import com.greendam.birdhelp.common.utils.AliOssUtil;
import com.greendam.birdhelp.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.*;

/**
 * <p>
 * 文件存储服务实现，自动选择本地磁盘或阿里云 OSS。
 * </p>
 *
 * <h3>存储选择策略</h3>
 * <ul>
 *   <li>若 {@link AliOssUtil} Bean 存在（OSS 已配置） → 使用阿里云 OSS</li>
 *   <li>否则 → 存储到本地磁盘 {@code app.upload-dir}</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    /** 本地存储根目录，默认当前项目下的 upload 目录 */
    @Value("${app.upload-dir:./upload}")
    private String uploadDir;

    /** OSS 工具类，未配置 OSS 时为 {@code null} */
    @Resource
    private AliOssUtil aliOssUtil;

    @Override
    public String store(byte[] content, String objectName) {
        if (aliOssUtil != null) {
            return aliOssUtil.upload(content, objectName);
        }
        return storeLocal(content, objectName);
    }

    @Override
    public byte[] load(String fileUrl) {
        if (aliOssUtil != null) {
            return loadFromOss(fileUrl);
        }
        return loadLocal(fileUrl);
    }

    @Override
    public void download(String fileUrl, HttpServletResponse response) throws IOException {
        if (aliOssUtil != null) {
            aliOssUtil.download(fileUrl, response);
        } else {
            downloadLocal(fileUrl, response);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (aliOssUtil != null) {
            aliOssUtil.delete(java.util.Collections.singletonList(fileUrl));
        } else {
            deleteLocal(fileUrl);
        }
    }

    // ==================== 本地存储 ====================

    private String storeLocal(byte[] content, String objectName) {
        try {
            Path filePath = Paths.get(uploadDir, objectName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("文件已保存到本地: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("本地文件写入失败: " + objectName, e);
        }
    }

    private byte[] loadLocal(String fileUrl) {
        try {
            Path path = Paths.get(fileUrl);
            if (!Files.exists(path)) {
                return null;
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取本地文件失败: {}", fileUrl, e);
            return null;
        }
    }

    private void downloadLocal(String fileUrl, HttpServletResponse response) throws IOException {
        Path path = Paths.get(fileUrl);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("文件不存在: " + fileUrl);
        }
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(path, out);
            out.flush();
        }
    }

    private void deleteLocal(String fileUrl) {
        try {
            Path path = Paths.get(fileUrl);
            Files.deleteIfExists(path);
            log.info("本地文件已删除: {}", fileUrl);
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", fileUrl, e);
        }
    }

    // ==================== OSS 存储 ====================

    private byte[] loadFromOss(String ossUrl) {
        // 简单实现：通过 HTTP 读取 OSS 文件内容
        try (InputStream in = new java.net.URL(ossUrl).openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("从 OSS 读取文件失败: {}", ossUrl, e);
            return null;
        }
    }
}
