package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.vo.QuotaInfoVO;
import com.greendam.birdhelp.service.QuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 额度模块接口控制器，提供用户端额度查询功能。
 * </p>
 *
 * <h3>路径说明</h3>
 * <ul>
 *   <li>所有接口均需携带有效 JWT Token</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/quota")
public class QuotaController {

    @Resource
    private QuotaService quotaService;

    /**
     * <p>查询当前登录用户的额度信息。</p>
     *
     * <p>返回今日剩余可用次数、每日上限、已用次数、当前会员等级及到期时间。</p>
     *
     * @return 额度信息视图
     */
    @GetMapping("/my")
    public BaseResponse<QuotaInfoVO> getMyQuota() {
        Long userId = BaseContext.getCurrentId();
        QuotaInfoVO vo = quotaService.getMyQuota(userId);
        return BaseResponse.success(vo);
    }
}
