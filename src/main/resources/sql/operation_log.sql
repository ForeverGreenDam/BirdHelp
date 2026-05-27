CREATE TABLE `operation_log`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `admin_id`    bigint      NOT NULL COMMENT '操作管理员ID',
    `admin_name`  varchar(50)          DEFAULT '' COMMENT '操作管理员用户名',
    `action`      varchar(50) NOT NULL COMMENT '操作类型',
    `target_type` varchar(50) NOT NULL COMMENT '操作目标类型',
    `target_id`   varchar(64)          DEFAULT '' COMMENT '操作目标ID',
    `detail`      varchar(500)         DEFAULT '' COMMENT '操作详情',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_action` (`action`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员操作日志表';
