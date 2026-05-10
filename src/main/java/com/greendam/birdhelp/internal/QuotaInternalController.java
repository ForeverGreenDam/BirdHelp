package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.QuotaOperateDTO;
import com.greendam.birdhelp.service.QuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 内部接口控制器，供 AI 模块调用，不对外暴露。
 * </p>
 *
 * <h3>鉴权说明</h3>
 * <p>当前版本暂不加 JWT 鉴权，后续可通过内部 Token 校验加强安全性。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class QuotaInternalController {

    @Resource
    private QuotaService quotaService;

    /**
     * <p>扣减额度，AI 模块生成文档前调用。</p>
     *
     * <p>校验额度可用后扣减一次，若额度已用完则返回错误。</p>
     *
     * @param dto 包含用户 ID 和关联业务 ID 的请求体
     * @return 扣减成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException {@code QUOTA_EXCEEDED} — 当日额度已用完
     */
    @PostMapping("/quota/consume")
    public BaseResponse<Void> consumeQuota(@Valid @RequestBody QuotaOperateDTO dto) {
        quotaService.consumeQuota(dto.getUserId(), dto.getRelatedId());
        return BaseResponse.success();
    }

    /**
     * <p>退还额度，AI 模块生成失败时调用。</p>
     *
     * <p>将已用次数回退 1 次（最低至 0）。</p>
     *
     * @param dto 包含用户 ID 和关联业务 ID 的请求体
     * @return 退还成功无数据返回
     */
    @PostMapping("/quota/refund")
    public BaseResponse<Void> refundQuota(@Valid @RequestBody QuotaOperateDTO dto) {
        quotaService.refundQuota(dto.getUserId(), dto.getRelatedId());
        return BaseResponse.success();
    }
}
