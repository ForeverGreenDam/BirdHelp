package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件服务接口，继承 MyBatis-Plus {@link IService} 获得通用 CRUD 能力。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface FileService extends IService<FileRecord> {

    /**
     * 用户上传文件。
     *
     * @param file      上传的文件
     * @param projectId 项目 ID
     * @param userId    用户 ID
     * @return 文件记录视图
     */
    FileRecordVO upload(MultipartFile file, Long projectId, Long userId);

    /**
     * AI 模块上传生成结果文件。
     *
     * @param content   文件字节内容
     * @param fileName  原始文件名
     * @param projectId 项目 ID
     * @param userId    用户 ID
     * @param versionOf 上一版本文件 ID（修改链，可选，原始生成时不传）
     * @return 文件记录视图
     */
    FileRecordVO uploadByAi(byte[] content, String fileName, Long projectId, Long userId, Long versionOf);

    /**
     * 查询文件记录（仅未删除、非回收站）。
     *
     * @param id 文件记录 ID
     * @return 文件记录视图
     */
    FileRecordVO getFileRecord(Long id);

    /**
     * 分页查询用户文件列表，支持按类型、来源筛选及关键词模糊搜索。
     *
     * @param page      页码
     * @param size      每页条数
     * @param fileType  文件类型（可选）：1-PPT 2-Word 3-PDF 4-图片 5-其他
     * @param source    文件来源（可选）：1-用户上传(知识库) 2-AI生成，{@code null} 表示全部
     * @param keyword   搜索关键词（可选），为空时不做文件名过滤
     * @param projectId 项目 ID
     * @param userId    用户 ID
     * @return 分页结果（仅链尾文件）
     */
    Page<FileRecordVO> listFiles(int page, int size, Integer fileType, Integer source,
                                 String keyword, Long projectId, Long userId);

    /**
     * 软删除文件（移入回收站）。
     *
     * @param id     文件记录 ID
     * @param userId 用户 ID
     */
    void softDelete(Long id, Long userId);

    /**
     * 从回收站恢复文件。
     *
     * @param id     文件记录 ID
     * @param userId 用户 ID
     */
    void restore(Long id, Long userId);

    /**
     * 永久删除文件（删除物理文件和数据库记录）。
     *
     * @param id     文件记录 ID
     * @param userId 用户 ID
     */
    void permanentDelete(Long id, Long userId);

    /**
     * 分页查询回收站文件列表。
     *
     * @param page      页码
     * @param size      每页条数
     * @param projectId 项目 ID
     * @param userId    用户 ID
     * @return 分页结果
     */
    Page<FileRecordVO> recycleList(int page, int size, Long projectId, Long userId);

    /**
     * 清理回收站中超过30天的过期文件（删除物理文件 + 数据库记录）。
     *
     * @return 清理的文件数量
     */
    int cleanExpiredRecycle();

    Page<FileRecord> adminListFiles(int page, int size, Long userId, Long projectId, String fileName, Integer fileType);

    void adminDeleteFile(Long id);
}
