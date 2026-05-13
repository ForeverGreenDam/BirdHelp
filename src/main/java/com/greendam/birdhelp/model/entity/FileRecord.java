package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 文件记录表实体类，映射数据表 {@code file_record}。
 * </p>
 *
 * <h3>回收站机制</h3>
 * <ul>
 *   <li>{@code deleted = 0}：正常文件</li>
 *   <li>{@code deleted = 1}：回收站文件，可恢复，30 天后定时任务自动清理</li>
 * </ul>
 * <p>{@code del_flag}（继承自 {@link BaseEntity}）仅用于 MyBatis-Plus 逻辑删除，
 * 作为永久删除标记，与回收站状态无关。</p>
 *
 * <h3>文件类型</h3>
 * <ul>
 *   <li>{@code fileType = 1}：PPT</li>
 *   <li>{@code fileType = 2}：Word</li>
 *   <li>{@code fileType = 3}：PDF</li>
 *   <li>{@code fileType = 4}：图片</li>
 *   <li>{@code fileType = 5}：其他</li>
 * </ul>
 *
 * <h3>文件来源</h3>
 * <ul>
 *   <li>{@code source = 1}：用户上传</li>
 *   <li>{@code source = 2}：AI 生成</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "file_record")
@Data
public class FileRecord extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 用户 ID，关联 {@code sys_user.id}。
     */
    private Long userId;

    /**
     * 项目 ID，关联 {@code project.id}。
     */
    private Long projectId;

    /**
     * 原始文件名（含扩展名）。
     */
    private String fileName;

    /**
     * 文件类型：{@code 1} - PPT，{@code 2} - Word，{@code 3} - PDF，{@code 4} - 图片，{@code 5} - 其他。
     */
    private Integer fileType;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件存储路径（本地磁盘绝对路径）或 OSS URL。
     */
    private String fileUrl;

    /**
     * 文件来源：{@code 1} - 用户上传，{@code 2} - AI 生成。
     */
    private Integer source;

    /**
     * 回收站标记：{@code 0} - 正常，{@code 1} - 回收站。
     */
    private Integer deleted;

    /**
     * 移入回收站的时间，用于 30 天自动清理判断。
     */
    private LocalDateTime deletedAt;
}
