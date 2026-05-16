package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【quota_config(额度配置表)】的数据库操作Mapper
 */
@Mapper
public interface QuotaConfigMapper extends BaseMapper<QuotaConfig> {
}
