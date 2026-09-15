package com.neteacher.user.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 家长视角的学情报告。
 */
@Data
public class ParentReport {

    private UserInfo child;

    private int assessmentCount;

    private int recordCount;

    /** 各科学情：{subject: score} */
    private Map<String, Integer> mastery;

    /** 自适应学习路径 */
    private com.neteacher.recommend.dto.RecommendPath path;

    /** AI 生成的家长寄语 */
    private String comment;
}
