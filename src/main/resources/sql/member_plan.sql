-- 会员套餐表
CREATE TABLE `member_plan` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '套餐名称',
    `level` tinyint NOT NULL COMMENT '会员等级，关联 quota_config.level',
    `price` decimal(10,2) NOT NULL COMMENT '原价（展示用）',
    `actual_price` decimal(10,2) NOT NULL COMMENT '实际售价',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐表';

-- 初始套餐数据（level 关联 quota_config，duration_days 和 daily_limit 由 quota_config 定义）
INSERT INTO `member_plan` (`name`, `level`, `price`, `actual_price`) VALUES
('免费用户', 0, 0.00, 0.00),
('月卡', 1, 29.90, 29.90),
('季卡', 2, 79.90, 79.90),
('年卡', 3, 299.00, 299.00);
