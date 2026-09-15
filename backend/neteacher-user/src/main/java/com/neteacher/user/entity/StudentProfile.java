package com.neteacher.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 学生档案（DE-003）：保存入学定级结果等学习基线。
 * 与用户账号一对一（按 student_id 唯一）。
 */
@Entity
@Table(name = "student_profile", uniqueConstraints = @UniqueConstraint(columnNames = "student_id"))
@Data
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    private Integer grade;

    private String goal;

    /** 入学测评定级得到的初始级别 L1-L6 */
    @Column(name = "init_level")
    private Integer initLevel;

    @Column(name = "eval_strictness")
    private Integer evalStrictness;
}
