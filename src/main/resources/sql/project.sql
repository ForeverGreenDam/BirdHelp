-- 项目表
CREATE TABLE `project` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '所属用户ID',
    `name` varchar(100) NOT NULL COMMENT '项目名称',
    `description` varchar(500) DEFAULT '' COMMENT '项目描述',
    `status` tinyint DEFAULT 1 COMMENT '状态 0-已归档 1-活跃',
    `file_count` int DEFAULT 0 COMMENT '文件数量（冗余缓存，避免 COUNT 查询）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';
