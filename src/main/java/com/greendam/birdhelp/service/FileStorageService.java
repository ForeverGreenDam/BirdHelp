package com.greendam.birdhelp.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
     * 下载文件并直接写入 HTTP 响应输出流（流式传输，不占用内存）。
     *
     * @param fileUrl  文件的存储 URL 或路径
     * @param response HTTP 响应
     * @throws IOException 当文件不存在或读取失败时抛出
     */
    void download(String fileUrl, HttpServletResponse response) throws IOException;

    /**
     * 删除存储的文件。
     *
     * @param fileUrl 文件的存储 URL 或路径
     */
    void delete(String fileUrl);
}
