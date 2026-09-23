package com.neteacher.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 学情看板：班级整体概览 + 学生明细。
 */
@Data
public class ClassOverview {
    private Long classId;
    private String className;
    private String schoolName;
    private Integer grade;
    private String headTeacherName;

    private int studentCount;
    /** 班级平均口语分 */
    private double avgSpeaking;
    /** 班级平均学习分钟 */
    private double avgMinutes;
    /** 今日打卡人数 */
    private int checkedTodayCount;
    /** 班级薄弱项 Top（按学科，出现次数降序） */
    private List<String> weakTopics;

    /** 班级薄弱知识点 Top（P3：按知识点粒度，出现次数降序） */
    private List<String> weakKnowledgePoints = new ArrayList<>();

    private List<StudentProgress> students;
}
