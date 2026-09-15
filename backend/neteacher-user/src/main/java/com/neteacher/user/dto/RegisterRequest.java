package com.neteacher.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求（DE-002 手机号注册）。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    /** STUDENT / PARENT / TEACHER */
    @NotBlank(message = "角色不能为空")
    private String role = "STUDENT";

    private Integer grade;

    /** 学生注册时可选择绑定家长手机号（可选） */
    private String parentPhone;
}
