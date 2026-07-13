-- 会员订单表
CREATE TABLE `member_order`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`    varchar(32)    NOT NULL COMMENT '订单号',
    `user_id`     bigint         NOT NULL COMMENT '用户ID',
    `plan_id`     bigint         NOT NULL COMMENT '套餐ID',
    `amount`      decimal(10, 2) NOT NULL COMMENT '支付金额',
    `pay_type`    tinyint        NOT NULL COMMENT '支付方式 1-微信 2-支付宝',
    `status`      tinyint                 DEFAULT 0 COMMENT '订单状态 0-待支付 1-已支付 2-已过期',
    `trade_no`    varchar(64)             DEFAULT '' COMMENT '支付宝交易号',
    `paid_at`     datetime                DEFAULT NULL COMMENT '支付成功时间',
    `expire_at`   datetime       NOT NULL COMMENT '订单过期时间',
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   varchar(64)             DEFAULT '' COMMENT '创建人',
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(64)             DEFAULT '' COMMENT '更新人',
    `del_flag`    tinyint                 DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='会员订单表';
