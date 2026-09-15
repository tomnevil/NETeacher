package com.neteacher.ops.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.ops.entity.Membership;
import com.neteacher.ops.entity.MembershipPlan;
import com.neteacher.ops.service.OpsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
@Tag(name = "运营与订单", description = "会员 / 活动")
public class OpsController {

    private final OpsService opsService;
    private final JwtUtil jwtUtil;

    @GetMapping("/plans")
    @Operation(summary = "会员套餐列表")
    public Result<List<MembershipPlan>> plans() {
        return Result.success(opsService.plans());
    }

    @PostMapping("/enroll")
    @Operation(summary = "报名会员套餐（FREE / PRO）")
    public Result<Membership> enroll(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String tier = body.get("tier");
        if (tier == null || tier.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "套餐类型不能为空");
        }
        return Result.success(opsService.enroll(tier, currentUid(request)));
    }

    @GetMapping("/mine")
    @Operation(summary = "我的会员状态")
    public Result<Membership> mine(HttpServletRequest request) {
        return Result.success(opsService.mine(currentUid(request)));
    }

    private Long currentUid(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        if (uid instanceof Long l) return l;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return jwtUtil.getUserId(auth.substring(7));
        throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
    }
}
