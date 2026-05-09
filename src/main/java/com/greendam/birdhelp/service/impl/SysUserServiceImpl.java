package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.service.SysUserService;
import com.greendam.birdhelp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author ForeverGreenDam
* @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
* @createDate 2026-05-10 01:52:40
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




