-- 文件记录表
CREATE TABLE `file_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `project_id` bigint NOT NULL COMMENT '所属项目ID',
    `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
    `file_type` tinyint NOT NULL COMMENT '文件类型 1-PPT 2-Word 3-PDF 4-图片 5-其他',
    `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_url` varchar(500) NOT NULL COMMENT '存储路径或OSS URL',
    `source` tinyint NOT NULL DEFAULT 1 COMMENT '来源 1-用户上传 2-AI生成',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '回收站标记 0-正常 1-回收站',
    `deleted_at` datetime DEFAULT NULL COMMENT '移入回收站时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';
