package com.neteacher.user.dto;

import lombok.Data;

/**
 * 班级视图（含学校名与学生数）。
 */
@Data
public class OrgClass {
    private Long id;
    private String name;
    private Long schoolId;
    private String schoolName;
    private Integer grade;
    private Long headTeacherId;
    private String headTeacherName;
    private int studentCount;
}
