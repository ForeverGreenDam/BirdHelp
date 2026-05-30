package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 单页预览数据视图对象。
 * </p>
 *
 * <h3>布局标注</h3>
 * <p>{@code layoutType} 和 {@code title} 来自 {@code file_record.outline} 的解析结果。
 * 旧文件无 outline 时这些字段可能为 {@code null}。</p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewPage {

    /**
     * 页码（从 1 开始）
     */
    private Integer pageNumber;

    /**
     * 预览图片 OSS URL
     */
    private String imageUrl;

    /**
     * 页面布局类型（从 outline 解析），如 "cover"、"big_number"、"text_only" 等
     */
    private String layoutType;

    /**
     * 页面标题（从 outline 解析）
     */
    private String title;
}
