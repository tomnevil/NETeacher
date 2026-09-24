package com.neteacher.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 作业统计（FR-TRK-010）：完成率、均分、薄弱知识点与学生明细。 */
@Data
public class AssignmentStatsDTO {

    private Long assignmentId;
    private String title;
    private Long paperId;
    private String paperTitle;
    private Long classId;
    private String className;

    /** 应完成人数（班级学员数） */
    private long assignedCount;
    /** 已完成人数 */
    private long completedCount;
    /** 完成率（%） */
    private double completionRate;
    /** 已完成者的平均分 */
    private double avgScore;

    /** 本次作业暴露出的薄弱知识点（按错误率降序） */
    private List<String> weakKnowledgePoints = new ArrayList<>();

    private List<StudentStat> students = new ArrayList<>();

    @Data
    public static class StudentStat {
        private Long studentId;
        private String nickname;
        private boolean finished;
        private Integer score;
    }
}
