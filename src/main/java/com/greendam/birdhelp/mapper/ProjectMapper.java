package com.greendam.birdhelp.mapper;

import com.greendam.birdhelp.model.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【project(项目表)】的数据库操作Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
