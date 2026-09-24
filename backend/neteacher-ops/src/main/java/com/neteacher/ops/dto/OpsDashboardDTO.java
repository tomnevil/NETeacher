package com.neteacher.ops.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 运营/质量看板（FR-OPS-008）。
 *
 * <p>指标口径与《PRD-主文档》§5 北极星及核心指标对齐，并一并返回目标值（targets），
 * 便于前端直接做「实际 vs 目标」对照。</p>
 */
@Data
public class OpsDashboardDTO {

    // ---------- 规模 ----------
    private long totalStudents;
    private long totalTeachers;

    // ---------- 活跃（北极星） ----------
    /** 今日活跃学员数 */
    private long dau;
    /** 近 7 日活跃学员数 */
    private long wau;
    /** 北极星：周活跃学员人均有效学习时长（分钟） */
    private double weeklyAvgMinutes;

    // ---------- 测评 ----------
    /** 单元测完成率（%） */
    private double unitTestCompletionRate;
    private long unitTestTotal;

    // ---------- 口语 ----------
    private double speakingAvgThisMonth;
    private double speakingAvgLastMonth;
    /** 口语跟读平均分月度提升（本月 - 上月） */
    private double speakingMonthlyDelta;

    // ---------- 留存 ----------
    /** 次周留存率（%）：上周活跃且本周仍活跃的学员 / 上周活跃学员 */
    private double retentionRate;

    // ---------- 商业化 ----------
    /** 免费 → 会员转化率（%） */
    private double membershipConversionRate;
    private long paidMembers;

    // ---------- 合规 ----------
    /** 未绑定家长手机号的学生账号占比（%），目标为 0 */
    private double unboundParentRate;
    private long unboundParentStudents;

    // ---------- 内容 ----------
    /** 题库使用率（%）：被测评引用过的已发布题目 / 已发布题目 */
    private double questionUsageRate;
    private long publishedQuestions;
    /** 课程使用率（%）：产生过学习记录的课程 / 课程总数 */
    private double courseUsageRate;
    private long totalCourses;

    /** PRD §5 目标值，键与本 DTO 的同名字段对应 */
    private Map<String, Double> targets = defaultTargets();

    private static Map<String, Double> defaultTargets() {
        Map<String, Double> t = new LinkedHashMap<>();
        t.put("weeklyAvgMinutes", 120.0);
        t.put("unitTestCompletionRate", 70.0);
        t.put("speakingMonthlyDelta", 5.0);
        t.put("retentionRate", 45.0);
        t.put("membershipConversionRate", 8.0);
        t.put("unboundParentRate", 0.0);
        return t;
    }
}
