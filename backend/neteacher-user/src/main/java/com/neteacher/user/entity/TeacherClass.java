package com.neteacher.user.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师-班级关联（组织模型 DE-ORG-003）：一名教师可任教多个班级，一个班级可有多名任课教师。
 */
@Getter
@Setter
@Entity
@Table(name = "teacher_class",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacherId", "classId"}))
public class TeacherClass extends BaseEntity {

    private Long teacherId;

    private Long classId;

    /** 任教学科：ENGLISH / MATH ... 缺省英语 */
    @Column(length = 16)
    private String subject = "ENGLISH";
}
