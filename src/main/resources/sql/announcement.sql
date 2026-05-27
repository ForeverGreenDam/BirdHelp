CREATE TABLE `announcement`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`        varchar(200) NOT NULL COMMENT '公告标题',
    `content`      text         NOT NULL COMMENT '公告内容',
    `status`       tinyint               DEFAULT 0 COMMENT '发布状态 0-草稿 1-已发布',
    `publish_time` datetime              DEFAULT NULL COMMENT '发布时间',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    varchar(64)           DEFAULT '' COMMENT '创建人',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`    varchar(64)           DEFAULT '' COMMENT '更新人',
    `del_flag`     tinyint               DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_publish_time` (`status`, `publish_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='公告表';
