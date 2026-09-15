package com.neteacher.user.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户账号（DE-001）。
 * 角色 role: STUDENT 学生 / PARENT 家长 / TEACHER 教师 / OPERATOR 运营
 * 状态 status: 0 游客 / 1 已绑定微信 / 2 正常 / 9 注销中
 */
@Getter
@Setter
@Entity
@Table(name = "user_account")
public class UserAccount extends BaseEntity {

    @Column(length = 20, unique = true, nullable = false)
    private String phone;

    /** 登录密码哈希（SHA-256 + 盐），存储格式 salt:hash */
    @Column(length = 100)
    private String password;

    @Column(length = 64)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    /** 学段/年级（整数编码，如 1~12） */
    private Integer grade;

    @Column(length = 16, nullable = false)
    private String role = "STUDENT";

    @Column(length = 64)
    private String wxOpenid;

    /** 家长账号 id（学生绑定家长时使用） */
    private Long parentId;

    /** 所属学校 id（组织模型）；教师与学生均可归属 */
    private Long schoolId;

    /** 所属班级 id（学生 / 班主任）；教师多班任教见 teacher_class */
    private Long classId;

    private Integer status = 2;
}
