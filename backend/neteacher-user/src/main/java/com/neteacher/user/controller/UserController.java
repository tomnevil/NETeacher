package com.neteacher.user.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.common.util.PasswordUtil;
import com.neteacher.user.dto.UserInfo;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.UserAccountRepository;
import com.neteacher.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * 用户个人中心（需登录）。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户", description = "个人中心 / 资料 / 家长绑定")
public class UserController {

    private final AuthService authService;
    private final UserAccountRepository userRepo;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    @Operation(summary = "当前登录用户信息")
    public Result<UserInfo> me(HttpServletRequest request) {
        return Result.success(authService.profile(currentUid(request)));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新昵称 / 头像")
    public Result<UserInfo> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return Result.success(authService.updateProfile(currentUid(request), body.get("nickname"), body.get("avatar")));
    }

    @PostMapping("/bind-parent")
    @Operation(summary = "绑定家长（按手机号）")
    public Result<UserInfo> bindParent(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long uid = currentUid(request);
        String parentPhone = body.get("parentPhone");
        if (parentPhone == null || parentPhone.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "家长手机号不能为空");
        }
        UserAccount self = userRepo.findById(uid)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        UserAccount parent = userRepo.findByPhone(parentPhone).orElseGet(() -> {
            UserAccount p = new UserAccount();
            p.setPhone(parentPhone);
            p.setPassword(PasswordUtil.hash("123456"));
            p.setNickname("家长" + parentPhone.substring(parentPhone.length() - 4));
            p.setRole("PARENT");
            p.setStatus(2);
            return userRepo.save(p);
        });
        if (!"PARENT".equals(parent.getRole())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "该手机号不是家长账号");
        }
        self.setParentId(parent.getId());
        userRepo.save(self);
        return Result.success(authService.profile(uid));
    }

    @GetMapping("/bind-status")
    @Operation(summary = "家长绑定状态")
    public Result<Map<String, Object>> bindStatus(HttpServletRequest request) {
        Long uid = currentUid(request);
        UserAccount self = userRepo.findById(uid)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (self.getParentId() == null) {
            return Result.success(Map.of("bound", false));
        }
        UserAccount parent = userRepo.findById(self.getParentId()).orElse(null);
        return Result.success(Map.of("bound", true,
                "parent", parent == null ? null : Map.of("phone", parent.getPhone(), "nickname", parent.getNickname())));
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
