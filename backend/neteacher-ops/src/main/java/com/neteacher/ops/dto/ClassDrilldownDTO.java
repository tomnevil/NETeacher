package com.neteacher.ops.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 运营看板：按班级下钻到学生明细（FR-OPS-008 扩展）。
 *
 * <p>在「等级 × 班级」的班级聚合之上，进一步给出每个学员的近 7 日学习时长、
 * 活跃天数、家长绑定情况与薄弱知识点，供运营/教师定位到具体人。</p>
 */
@Data
public class ClassDrilldownDTO {

    private Long classId;
    private String className;
    private long students;
    private double weeklyAvgMinutes;
    private long unboundParentCount;

    private List<StudentRow> rows = new ArrayList<>();

    @Data
    public static class StudentRow {
        private Long studentId;
        private String nickname;

        /** 近 7 日学习时长（分钟） */
        private double weeklyMinutes;
        /** 近 7 日活跃天数 */
        private long activeDays;
        /** 最近一次学习时间 */
        private LocalDateTime lastActiveAt;

        /** 未绑定家长（合规风险项） */
        private boolean unboundParent;

        /** 薄弱知识点（正确率升序，最弱在前） */
        private List<String> weakKnowledgePoints = new ArrayList<>();
    }
}
