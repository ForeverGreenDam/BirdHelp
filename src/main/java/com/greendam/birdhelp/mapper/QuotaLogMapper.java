package com.greendam.birdhelp.mapper;

import com.greendam.birdhelp.model.entity.QuotaLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【quota_log(额度流水表)】的数据库操作Mapper
 */
@Mapper
public interface QuotaLogMapper extends BaseMapper<QuotaLog> {
}
