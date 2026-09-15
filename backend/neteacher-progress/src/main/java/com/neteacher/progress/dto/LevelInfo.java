package com.neteacher.progress.dto;

import lombok.Data;

@Data
public class LevelInfo {
    private String lv;
    private String name;
    private boolean unlocked;
    private int stars;
    private boolean current;
    private int courseCount;
    private int completedCount;
    /** 该关卡首门课程 ID（用于点击节点直达练习页），为 0 表示暂无课程 */
    private Long firstCourseId;
    private String firstCourseTitle;
}
