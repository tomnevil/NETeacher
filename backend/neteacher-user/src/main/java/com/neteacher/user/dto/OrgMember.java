package com.neteacher.user.dto;

import lombok.Data;

/**
 * 班级成员（学生 / 教师）简要信息，用于后台绑定与名单展示。
 */
@Data
public class OrgMember {
    private Long uid;
    private String nickname;
    private String phone;
    private String role;
    private Integer grade;
    private Long classId;
    private String className;
}
