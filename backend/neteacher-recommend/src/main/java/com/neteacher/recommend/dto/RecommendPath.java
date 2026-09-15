package com.neteacher.recommend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 自适应学习路径（基于测评与学习记录计算）。
 */
@Data
public class RecommendPath {

    private Integer currentLevel;

    private Integer nextLevel;

    /** 各科学情：{subject: score} */
    private Map<String, Integer> mastery;

    private List<RecommendItem> items;
}
