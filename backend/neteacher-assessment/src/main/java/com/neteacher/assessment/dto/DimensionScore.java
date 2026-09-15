package com.neteacher.assessment.dto;

import lombok.Data;

/**
 * 能力雷达图的一个维度得分（0-100）。
 */
@Data
public class DimensionScore {
    private String dimension; // listening / speaking / reading / writing / word / grammar
    private String label;     // 中文维度名
    private int score;
}
