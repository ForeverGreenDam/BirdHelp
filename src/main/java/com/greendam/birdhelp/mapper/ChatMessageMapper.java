package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息表 Mapper 接口，继承 MyBatis-Plus {@link BaseMapper}，自动获得通用 CRUD 方法。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
