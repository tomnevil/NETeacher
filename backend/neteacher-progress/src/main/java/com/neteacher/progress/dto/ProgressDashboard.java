package com.neteacher.progress.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 学习仪表盘聚合数据。
 */
@Data
public class ProgressDashboard {
    /** 综合等级 L1-L6 */
    private int overallLevel;
    /** 各科学情 {subject: score} */
    private Map<String, Integer> mastery;
    /** 连续打卡天数 */
    private int streakDays;
    /** 累计练习时长（分钟） */
    private int totalMinutes;
    /** 测评次数 */
    private int assessmentCount;
    /** 练习次数 */
    private int recordCount;
    /** 口语平均得分 */
    private double speakingAvg;
    /** 薄弱学科 */
    private List<String> weakSubjects;
    /** 最近练习记录（最多 5 条） */
    private List<RecentRecord> recentRecords;

    @Data
    public static class RecentRecord {
        private Long id;
        private String module;
        private Integer score;
        private Integer durationSec;
        private String createdAt;
    }
}
