package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import com.greendam.birdhelp.service.FileService;
import com.greendam.birdhelp.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 文件服务实现类，继承 MyBatis-Plus {@link ServiceImpl} 提供通用 CRUD 实现。
 * </p>
 *
 * <h3>回收站机制</h3>
 * <ul>
 *   <li>软删除：设置 {@code deleted=1, deletedAt=now()}，文件仍在存储中</li>
 *   <li>恢复：设置 {@code deleted=0, deletedAt=null}</li>
 *   <li>永久删除：删除物理文件 + 逻辑删除数据库记录</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord>
        implements FileService {

    @Resource
    private FileStorageService fileStorageService;

    /** 回收站保留天数 */
    private static final int RECYCLE_DAYS = 30;

    /** 文件类型到目录名的映射 */
    private static final Map<Integer, String> TYPE_DIR_MAP = new HashMap<>();
    static {
        TYPE_DIR_MAP.put(1, "ppt");
        TYPE_DIR_MAP.put(2, "word");
        TYPE_DIR_MAP.put(3, "pdf");
        TYPE_DIR_MAP.put(4, "image");
        TYPE_DIR_MAP.put(5, "other");
    }

    /** 扩展名到文件类型的映射 */
    private static final Map<String, Integer> EXT_TYPE_MAP = new HashMap<>();
    static {
        EXT_TYPE_MAP.put("ppt", 1);
        EXT_TYPE_MAP.put("pptx", 1);
        EXT_TYPE_MAP.put("doc", 2);
        EXT_TYPE_MAP.put("docx", 2);
        EXT_TYPE_MAP.put("pdf", 3);
        EXT_TYPE_MAP.put("jpg", 4);
        EXT_TYPE_MAP.put("jpeg", 4);
        EXT_TYPE_MAP.put("png", 4);
        EXT_TYPE_MAP.put("gif", 4);
        EXT_TYPE_MAP.put("bmp", 4);
        EXT_TYPE_MAP.put("webp", 4);
    }

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // ==================== 公开方法 ====================

    @Override
    public FileRecordVO upload(MultipartFile file, Long projectId, Long userId) {
        try {
            return doUpload(file.getBytes(), file.getOriginalFilename(), projectId, userId, 1);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件读取失败");
        }
    }

    @Override
    public FileRecordVO uploadByAi(byte[] content, String fileName, Long projectId, Long userId) {
        return doUpload(content, fileName, projectId, userId, 2);
    }

    @Override
    public FileRecordVO getFileRecord(Long id) {
        FileRecord record = getById(id);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        return toVO(record);
    }

    @Override
    public Page<FileRecordVO> listFiles(int page, int size, Integer fileType, Long projectId, Long userId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getProjectId, projectId)
                .eq(FileRecord::getDeleted, 0)
                .eq(fileType != null, FileRecord::getFileType, fileType)
                .orderByDesc(FileRecord::getCreateTime);

        Page<FileRecord> resultPage = page(new Page<>(page, size), wrapper);
        return convertPage(resultPage);
    }

    @Override
    public Page<FileRecordVO> searchFiles(String keyword, int page, int size, Long projectId, Long userId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getProjectId, projectId)
                .eq(FileRecord::getDeleted, 0)
                .like(FileRecord::getFileName, keyword)
                .orderByDesc(FileRecord::getCreateTime);

        Page<FileRecord> resultPage = page(new Page<>(page, size), wrapper);
        return convertPage(resultPage);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        FileRecord record = getOwnRecord(id, userId);
        if (record.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件已在回收站中");
        }
        record.setDeleted(1);
        record.setDeletedAt(LocalDateTime.now());
        updateById(record);
        log.info("文件移入回收站: id={}, userId={}", id, userId);
    }

    @Override
    public void restore(Long id, Long userId) {
        FileRecord record = getOwnRecord(id, userId);
        if (record.getDeleted() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件不在回收站中");
        }
        record.setDeleted(0);
        record.setDeletedAt(null);
        updateById(record);
        log.info("从回收站恢复文件: id={}, userId={}", id, userId);
    }

    @Override
    public void permanentDelete(Long id, Long userId) {
        FileRecord record = getOwnRecord(id, userId);
        fileStorageService.delete(record.getFileUrl());
        removeById(id);
        log.info("永久删除文件: id={}, userId={}", id, userId);
    }

    @Override
    public Page<FileRecordVO> recycleList(int page, int size, Long projectId, Long userId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getProjectId, projectId)
                .eq(FileRecord::getDeleted, 1)
                .orderByDesc(FileRecord::getDeletedAt);

        Page<FileRecord> resultPage = page(new Page<>(page, size), wrapper);
        return convertPage(resultPage);
    }

    // ==================== 内部方法 ====================

    /**
     * 核心上传逻辑：保存文件到存储 → 插入数据库记录。
     */
    private FileRecordVO doUpload(byte[] content, String originalName, Long projectId, Long userId, int source) {
        String ext = extractExt(originalName);
        int fileType = resolveFileType(ext);
        String objectName = buildObjectName(projectId, fileType, ext);

        String fileUrl = fileStorageService.store(content, objectName);

        FileRecord record = new FileRecord();
        record.setUserId(userId);
        record.setProjectId(projectId);
        record.setFileName(originalName);
        record.setFileType(fileType);
        record.setFileSize((long) content.length);
        record.setFileUrl(fileUrl);
        record.setSource(source);
        record.setDeleted(0);
        save(record);

        log.info("文件上传成功: id={}, projectId={}, fileName={}, fileType={}, source={}",
                record.getId(), projectId, originalName, fileType, source);
        return toVO(record);
    }

    /**
     * 获取用户拥有的文件记录，不存在或不属于该用户时抛异常。
     */
    private FileRecord getOwnRecord(Long id, Long userId) {
        FileRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权操作该文件");
        }
        return record;
    }

    /**
     * 根据扩展名确定文件类型。
     */
    private int resolveFileType(String ext) {
        return EXT_TYPE_MAP.getOrDefault(ext.toLowerCase(), 5);
    }

    /**
     * 提取文件扩展名。
     */
    private String extractExt(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx < 0 ? "" : fileName.substring(dotIdx + 1);
    }

    /**
     * 构建存储对象名：{project_id}/{type_dir}/{yyyy-MM}/{uuid}.{ext}
     */
    private String buildObjectName(Long projectId, int fileType, String ext) {
        String typeDir = TYPE_DIR_MAP.getOrDefault(fileType, "other");
        String month = LocalDateTime.now().format(MONTH_FMT);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return projectId + "/" + typeDir + "/" + month + "/" + uuid + "." + ext;
    }

    private FileRecordVO toVO(FileRecord record) {
        return FileRecordVO.builder()
                .id(record.getId())
                .projectId(record.getProjectId())
                .fileName(record.getFileName())
                .fileType(record.getFileType())
                .fileSize(record.getFileSize())
                .source(record.getSource())
                .deleted(record.getDeleted())
                .deletedAt(record.getDeletedAt())
                .createTime(record.getCreateTime())
                .build();
    }

    @Override
    public int cleanExpiredRecycle() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RECYCLE_DAYS);
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getDeleted, 1)
                .le(FileRecord::getDeletedAt, threshold);

        List<FileRecord> expiredRecords = list(wrapper);
        int count = 0;
        for (FileRecord record : expiredRecords) {
            try {
                fileStorageService.delete(record.getFileUrl());
            } catch (Exception e) {
                log.warn("清理过期文件失败，跳过: id={}, url={}", record.getId(), record.getFileUrl(), e);
                continue;
            }
            removeById(record.getId());
            count++;
        }
        if (count > 0) {
            log.info("回收站清理完成，共清理 {} 个过期文件", count);
        }
        return count;
    }

    private Page<FileRecordVO> convertPage(Page<FileRecord> entityPage) {
        Page<FileRecordVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<FileRecordVO> voList = new ArrayList<>();
        for (FileRecord record : entityPage.getRecords()) {
            voList.add(toVO(record));
        }
        voPage.setRecords(voList);
        return voPage;
    }
}
