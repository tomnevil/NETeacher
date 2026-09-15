package com.neteacher.user.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 班级（组织模型 DE-ORG-002）。
 * 一个班级归属一所学校，并设定年级，用于按年级下发课程内容。
 */
@Getter
@Setter
@Entity
@Table(name = "class_group")
public class ClassGroup extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String name;

    private Long schoolId;

    /** 年级（1~12，与 UserAccount.grade 对齐） */
    private Integer grade;

    /** 班主任 / 负责教师 id（关联 user_account.id） */
    private Long headTeacherId;

    /** 状态：1 正常 / 0 停用 */
    private Integer status = 1;
}
