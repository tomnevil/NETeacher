package com.neteacher.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求：手机号 + 密码。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
