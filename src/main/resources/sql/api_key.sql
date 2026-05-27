CREATE TABLE `api_key`
(
    `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `provider_name` varchar(50)   NOT NULL COMMENT '供应商名称',
    `api_key`       varchar(2000) NOT NULL COMMENT '加密后的API密钥值',
    `base_url`      varchar(500)           DEFAULT '' COMMENT 'API基础地址',
    `model_name`    varchar(100)           DEFAULT '' COMMENT '关联模型名称',
    `enabled`       tinyint                DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    `description`   varchar(255)           DEFAULT '' COMMENT '备注说明',
    `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`     varchar(64)            DEFAULT '' COMMENT '创建人',
    `update_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`     varchar(64)            DEFAULT '' COMMENT '更新人',
    `del_flag`      tinyint                DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_provider_name` (`provider_name`),
    KEY `idx_enabled` (`enabled`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='LLM API密钥配置表';
