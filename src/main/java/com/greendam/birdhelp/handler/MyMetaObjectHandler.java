package com.greendam.birdhelp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.greendam.birdhelp.common.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * <p>
 * MyBatis-Plus 自动填充处理器，在 INSERT 和 UPDATE 操作时自动填充审计字段。
 * </p>
 *
 * <h3>填充策略</h3>
 * <table border="1">
 *   <caption>字段填充时机</caption>
 *   <tr><th>字段</th><th>INSERT</th><th>UPDATE</th><th>填充值来源</th></tr>
 *   <tr><td>{@code createTime}</td><td align="center">&#10003;</td><td align="center">&#10008;</td><td>{@link LocalDateTime#now()}</td></tr>
 *   <tr><td>{@code createBy}</td><td align="center">&#10003;</td><td align="center">&#10008;</td><td>{@link BaseContext#getCurrentId()}</td></tr>
 *   <tr><td>{@code updateTime}</td><td align="center">&#10003;</td><td align="center">&#10003;</td><td>{@link LocalDateTime#now()}</td></tr>
 *   <tr><td>{@code updateBy}</td><td align="center">&#10003;</td><td align="center">&#10003;</td><td>{@link BaseContext#getCurrentId()}</td></tr>
 * </table>
 *
 * <p>仅当目标字段值为 {@code null} 时才会填充（{@code strictFill} 模式），不会覆盖已有值。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * INSERT 操作时自动填充审计字段。
     *
     * <p>填充 {@code createTime}、{@code createBy}、{@code updateTime}、{@code updateBy} 四个字段。</p>
     *
     * @param metaObject MyBatis-Plus 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始自动填充插入字段...");
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        String userId = currentId != null ? currentId.toString() : null;

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, userId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", String.class, userId);
    }

    /**
     * UPDATE 操作时自动填充审计字段。
     *
     * <p>填充 {@code updateTime}、{@code updateBy} 两个字段。</p>
     *
     * @param metaObject MyBatis-Plus 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始自动填充更新字段...");
        Long currentId = BaseContext.getCurrentId();
        String userId = currentId != null ? currentId.toString() : null;

        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, userId);
    }
}
