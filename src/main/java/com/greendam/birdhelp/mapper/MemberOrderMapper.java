package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.MemberOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【member_order(会员订单表)】的数据库操作Mapper
 */
@Mapper
public interface MemberOrderMapper extends BaseMapper<MemberOrder> {
}
