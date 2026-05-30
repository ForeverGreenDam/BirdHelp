package com.greendam.birdhelp.service;

import com.greendam.birdhelp.model.vo.PreviewVO;

/**
 * <p>
 * 文件预览服务接口。
 * </p>
 *
 * <h3>渲染管道</h3>
 * <ol>
 *   <li>查出 {@code file_record} 获取文件路径 + outline + preview_pages</li>
 *   <li>三级缓存判断：Redis → MySQL → 重新渲染</li>
 *   <li>渲染：LibreOffice 无头转 PDF → PDFBox 逐页渲染 150 DPI PNG → 上传 OSS</li>
 *   <li>持久化：更新 {@code file_record.preview_pages}，Redis 热缓存 1h</li>
 * </ol>
 *
 * <h3>布局标注</h3>
 * <p>每页的 {@code layoutType} / {@code title} 标注来自 {@code file_record.outline} 的解析结果。</p>
 *
 * @author ForeverGreenDam
 */
public interface PreviewService {

    /**
     * 获取文件的预览数据（多级缓存）。
     *
     * @param fileId 文件记录 ID
     * @return 预览数据（包含每页的图片 URL 和布局标注）
     */
    PreviewVO getPreview(Long fileId);

    /**
     * 强制重新渲染预览（清除缓存后重新生成）。
     *
     * @param fileId 文件记录 ID
     * @return 重新渲染后的预览数据
     */
    PreviewVO refreshPreview(Long fileId);
}
