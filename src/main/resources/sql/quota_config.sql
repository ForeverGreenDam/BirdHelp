-- 额度配置表，后台管理维护
CREATE TABLE `quota_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `level` tinyint NOT NULL COMMENT '会员等级 0-免费 1-月卡 2-季卡 3-年卡',
    `daily_limit` int NOT NULL COMMENT '每日生成次数上限',
    `duration_days` int NOT NULL DEFAULT 0 COMMENT '有效天数（0表示不限，仅免费等级使用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='额度配置表';

-- 初始额度配置数据
INSERT INTO `quota_config` (`level`, `daily_limit`, `duration_days`)
VALUES (0, 10, 0);
INSERT INTO `quota_config` (`level`, `daily_limit`, `duration_days`)
VALUES (1, 30, 30);
INSERT INTO `quota_config` (`level`, `daily_limit`, `duration_days`)
VALUES (2, 60, 90);
INSERT INTO `quota_config` (`level`, `daily_limit`, `duration_days`)
VALUES (3, 100, 365);
