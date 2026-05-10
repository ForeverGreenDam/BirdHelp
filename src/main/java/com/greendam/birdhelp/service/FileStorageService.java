package com.greendam.birdhelp.service;

import java.io.InputStream;

/**
 * <p>
 * 文件存储服务接口，屏蔽底层存储差异（本地磁盘 / 阿里云 OSS）。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface FileStorageService {

    /**
     * 存储文件并返回访问 URL。
     *
     * @param content    文件字节内容
     * @param objectName 存储对象名（如 ppt/2026-05/uuid.pptx）
     * @return 文件访问 URL（本地路径或 OSS URL）
     */
    String store(byte[] content, String objectName);

    /**
     * 读取文件内容，用于下载。
     *
     * @param fileUrl 文件的存储 URL 或路径
     * @return 文件字节内容，不存在时返回 {@code null}
     */
    byte[] load(String fileUrl);

    /**
     * 删除存储的文件。
     *
     * @param fileUrl 文件的存储 URL 或路径
     */
    void delete(String fileUrl);
}
