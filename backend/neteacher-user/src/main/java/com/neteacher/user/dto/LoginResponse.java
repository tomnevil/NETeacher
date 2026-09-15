package com.neteacher.user.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private Long uid;

    private String nickname;

    private String role;

    /** 年级（可能为 null，用于口语对话按年级发起话题）。 */
    private Integer grade;
}
