-- 用户额度表
CREATE TABLE `user_quota` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `member_level` tinyint DEFAULT 0 COMMENT '会员等级 0-免费 1-月卡 2-季卡 3-年卡',
    `member_expire_at` datetime DEFAULT NULL COMMENT '会员到期时间',
    `daily_used` int DEFAULT 0 COMMENT '今日已用次数',
    `daily_date` date DEFAULT NULL COMMENT '已用次数对应日期，用于跨天判断',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户额度表';
