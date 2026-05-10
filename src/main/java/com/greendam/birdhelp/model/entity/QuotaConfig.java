package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 额度配置表实体类，映射数据表 {@code quota_config}。
 * 由后台管理员维护，定义各会员等级每日生成次数上限。
 * </p>
 *
 * <h3>预设配置</h3>
 * <ul>
 *   <li>免费用户（level=0）：每日 10 次</li>
 *   <li>月卡（level=1）：每日 30 次</li>
 *   <li>季卡（level=2）：每日 60 次</li>
 *   <li>年卡（level=3）：每日 100 次</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "quota_config")
@Data
public class QuotaConfig extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 会员等级：{@code 0} - 免费，{@code 1} - 月卡，{@code 2} - 季卡，{@code 3} - 年卡。
     */
    private Integer level;

    /**
     * 每日生成次数上限。
     */
    private Integer dailyLimit;
}
