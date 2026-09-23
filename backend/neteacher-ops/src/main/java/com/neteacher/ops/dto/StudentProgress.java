package com.neteacher.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 学情看板：单个学生的进度概览。
 */
@Data
public class StudentProgress {
    private Long studentId;
    private String nickname;
    private String phone;
    private Integer grade;

    /** 综合等级 L1-L6 */
    private int overallLevel;
    /** 口语平均分 */
    private double speakingAvg;
    /** 累计学习分钟 */
    private int totalMinutes;
    /** 连续打卡天数 */
    private int streakDays;
    /** 完成课程数 */
    private int completedCourses;

    /** 薄弱知识点（测评均分<60 的科目） */
    private List<String> weakSubjects;

    /** 薄弱知识点（P3：按知识点聚合正确率 <60% 且样本 >=2 的标签，最弱在前） */
    private List<String> weakKnowledgePoints = new ArrayList<>();
    /** 是否今日已打卡 */
    private boolean checkedToday;
}
