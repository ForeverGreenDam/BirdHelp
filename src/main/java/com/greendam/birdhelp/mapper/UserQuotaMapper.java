package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.UserQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【user_quota(用户额度表)】的数据库操作Mapper
 */
@Mapper
public interface UserQuotaMapper extends BaseMapper<UserQuota> {
}
