package com.neteacher.user.dto;

import lombok.Data;

/**
 * 学校视图（含班级数与师生数）。
 */
@Data
public class OrgSchool {
    private Long id;
    private String name;
    private String stage;
    private String city;
    private int classCount;
    private int teacherCount;
    private int studentCount;
}
