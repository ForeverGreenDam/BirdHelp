CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键用户ID',
                            `tenant_id` bigint DEFAULT 0 COMMENT '租户ID，多租户预留',
                            `username` varchar(50) NOT NULL COMMENT '登录账号',
                            `nickname` varchar(50) NOT NULL COMMENT '用户昵称',
                            `password` varchar(100) NOT NULL COMMENT '加密密码',
                            `avatar` varchar(255) DEFAULT '' COMMENT '头像地址',
                            `phone` varchar(20) DEFAULT '' COMMENT '手机号',
                            `email` varchar(100) DEFAULT '' COMMENT '邮箱',
                            `sex` tinyint DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
                            `birthday` date DEFAULT NULL COMMENT '出生日期',
                            `user_type` tinyint DEFAULT 1 COMMENT '用户类型 1-普通学生 2-管理员',
                            `status` tinyint DEFAULT 1 COMMENT '账号状态 0-禁用 1-正常',
                            `wx_openid` varchar(100) DEFAULT '' COMMENT '微信openid',
                            `wx_unionid` varchar(100) DEFAULT '' COMMENT '微信unionid',

    -- 标准审计字段
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
                            `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',

                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_phone` (`phone`),
                            UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';