package com.neteacher.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户视图对象（脱敏，对外返回）。
 */
@Data
public class UserInfo implements Serializable {

    private Long uid;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer grade;

    private String role;

    private Integer status;
}
