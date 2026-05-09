package com.greendam.birdhelp.mapper;

import com.greendam.birdhelp.model.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

/**
* @author ForeverGreenDam
* @description 针对表【sys_user(用户信息表)】的数据库操作Mapper
* @createDate 2026-05-10 01:52:40
* @Entity com.greendam.birdhelp.model.entity.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}




