package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 文件预览响应视图对象。
 * </p>
 *
 * <h3>缓存策略</h3>
 * <ol>
 *   <li>Redis 热缓存（{@code preview:{fileId}}，TTL 1h）→ fileHash 一致直接返回</li>
 *   <li>MySQL {@code file_record.preview_pages} → fileHash 一致返回 + 回填 Redis</li>
 *   <li>都不命中 → 重新渲染</li>
 * </ol>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewVO {

    /**
     * 文件记录 ID
     */
    private Long fileId;

    /**
     * 文件哈希值（用于缓存校验）
     */
    private String fileHash;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 每页预览数据
     */
    private List<PreviewPage> pages;
}
