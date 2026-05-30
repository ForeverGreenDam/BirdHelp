-- 对话消息明细表
-- 对应设计文档 §四.4.1

CREATE TABLE `chat_message`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`  varchar(64) NOT NULL COMMENT '关联 chat_session.session_id',
    `role`        varchar(16) NOT NULL COMMENT '消息角色：user / assistant',
    `content`     TEXT        NOT NULL COMMENT '消息内容（用户消息或 AI 回复）',
    `file_id`     bigint               DEFAULT NULL COMMENT '关联的文件 ID（assistant 消息可选关联，记录该轮产出的文件版本）',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_time` (`session_id`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='对话消息明细表';
