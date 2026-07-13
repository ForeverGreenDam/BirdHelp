package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.admin.MemberPlanCreateDTO;
import com.greendam.birdhelp.model.dto.admin.MemberPlanUpdateDTO;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.vo.admin.AdminMemberOrderVO;
import com.greendam.birdhelp.service.admin.AdminMemberService;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 管理员会员管理接口控制器，提供套餐管理、订单查询、手动授予会员等功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>套餐管理：查询、新增、修改、上架/下架会员套餐</li>
 *   <li>订单管理：分页查询订单列表、查看订单详情</li>
 *   <li>手动授予会员：为指定用户手动激活会员</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/member")
public class AdminMemberController {

    @Resource
    private AdminMemberService adminMemberService;

    @Resource
    private OperationLogService operationLogService;

    // ==================== 套餐管理 ====================

    /**
     * <p>查询所有套餐列表。</p>
     *
     * @return 套餐列表
     */
    @GetMapping("/plan/list")
    public BaseResponse<List<MemberPlan>> listPlans() {
        return BaseResponse.success(adminMemberService.listPlans());
    }

    /**
     * <p>查询套餐详情。</p>
     *
     * @param id 套餐 ID
     * @return 套餐详情
     */
    @GetMapping("/plan/{id}")
    public BaseResponse<MemberPlan> getPlanDetail(@PathVariable Long id) {
        return BaseResponse.success(adminMemberService.getPlanDetail(id));
    }

    /**
     * <p>新增套餐。</p>
     *
     * @param dto 创建套餐参数
     * @return 操作成功无数据返回
     */
    @PostMapping("/plan")
    public BaseResponse<Void> createPlan(@Valid @RequestBody MemberPlanCreateDTO dto) {
        adminMemberService.createPlan(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "CREATE", "member_plan", null, "新增套餐: " + dto.getName());
        return BaseResponse.success();
    }

    /**
     * <p>修改套餐。</p>
     *
     * @param dto 修改套餐参数
     * @return 操作成功无数据返回
     */
    @PutMapping("/plan")
    public BaseResponse<Void> updatePlan(@Valid @RequestBody MemberPlanUpdateDTO dto) {
        adminMemberService.updatePlan(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "member_plan", dto.getId().toString(), "修改套餐");
        return BaseResponse.success();
    }

    /**
     * <p>上架/下架套餐。</p>
     *
     * @param id     套餐 ID
     * @param status 目标状态：0-下架 1-上架
     * @return 操作成功无数据返回
     */
    @PutMapping("/plan/{id}/status")
    public BaseResponse<Void> updatePlanStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminMemberService.updatePlanStatus(id, status);
        String action = status == 1 ? "上架" : "下架";
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "member_plan", id.toString(), action + "套餐");
        return BaseResponse.success();
    }

    // ==================== 订单管理 ====================

    /**
     * <p>分页查询订单列表。</p>
     *
     * <p>支持按用户 ID 和订单状态进行筛选。</p>
     *
     * @param page   页码，默认为 1
     * @param size   每页条数，默认为 10
     * @param userId 用户 ID（可选）
     * @param status 订单状态（可选）：0-待支付 1-已支付 2-已过期
     * @return 订单分页数据
     */
    @GetMapping("/order/list")
    public BaseResponse<Page<AdminMemberOrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return BaseResponse.success(adminMemberService.listOrders(page, size, userId, status));
    }

    /**
     * <p>查询订单详情。</p>
     *
     * @param id 订单 ID
     * @return 订单详情
     */
    @GetMapping("/order/{id}")
    public BaseResponse<AdminMemberOrderVO> getOrderDetail(@PathVariable Long id) {
        return BaseResponse.success(adminMemberService.getOrderDetail(id));
    }
}
