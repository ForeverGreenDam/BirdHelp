package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【file_record(文件记录表)】的数据库操作Mapper
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
