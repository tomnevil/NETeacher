package com.neteacher.user.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.user.dto.ParentReport;
import com.neteacher.user.dto.UserInfo;
import com.neteacher.user.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家长端（M1）：孩子列表与学情报告。
 */
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@Tag(name = "家长", description = "孩子列表 / 学情报告")
public class ParentController {

    private final ParentService parentService;
    private final JwtUtil jwtUtil;

    @GetMapping("/children")
    @Operation(summary = "我绑定的孩子列表")
    public Result<List<UserInfo>> children(HttpServletRequest request) {
        return Result.success(parentService.children(currentUid(request)));
    }

    @GetMapping("/report/{childId}")
    @Operation(summary = "孩子学情报告")
    public Result<ParentReport> report(@PathVariable Long childId, HttpServletRequest request) {
        return Result.success(parentService.report(currentUid(request), childId));
    }

    private Long currentUid(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        if (uid instanceof Long l) {
            return l;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.getUserId(auth.substring(7));
        }
        throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
    }
}
