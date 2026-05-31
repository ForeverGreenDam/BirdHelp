-- 对话修改会话表
-- 对应设计文档 §四.4.1

CREATE TABLE `chat_session`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`       varchar(64) NOT NULL COMMENT '会话 ID（UUID v4），对外暴露',
    `user_id`          bigint      NOT NULL COMMENT '用户 ID，关联 sys_user.id',
    `project_id`       bigint      NOT NULL COMMENT '项目 ID，关联 project.id',
    `original_file_id` bigint      NOT NULL COMMENT '修改的起点文件 ID（用户点击修改按钮时的文件），关联 file_record.id',
    `current_file_id`  bigint               DEFAULT NULL COMMENT '当前最新版本文件 ID（每次修改后更新），关联 file_record.id',
    `doc_type`         varchar(10) NOT NULL COMMENT '文档类型：ppt / word / pdf',
    `title`     varchar(200) DEFAULT '' COMMENT '会话标题（取自原始文件名，支持后续重命名）',
    `message_count`    int         NOT NULL DEFAULT 0 COMMENT '消息总数（冗余，避免 COUNT 查询）',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64)  DEFAULT '' COMMENT '创建人',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `update_by` varchar(64)  DEFAULT '' COMMENT '更新人',
    `del_flag`         tinyint              DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_original_file_id` (`original_file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='对话修改会话表';
