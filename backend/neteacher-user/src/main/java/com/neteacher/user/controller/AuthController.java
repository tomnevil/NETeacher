package com.neteacher.user.controller;

import com.neteacher.common.result.Result;
import com.neteacher.user.dto.LoginRequest;
import com.neteacher.user.dto.LoginResponse;
import com.neteacher.user.dto.RegisterRequest;
import com.neteacher.user.dto.UserInfo;
import com.neteacher.user.dto.WechatLoginRequest;
import com.neteacher.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关接口（无需登录令牌）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证", description = "登录 / 注册")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "手机号 + 密码登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/register")
    @Operation(summary = "手机号 + 密码注册")
    public Result<UserInfo> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    @PostMapping("/wechat")
    @Operation(summary = "微信登录（code 换取令牌；未配置凭证时降级为演示登录）")
    public Result<LoginResponse> wechat(@Valid @RequestBody WechatLoginRequest req) {
        return Result.success(authService.wechatLogin(req));
    }
}
