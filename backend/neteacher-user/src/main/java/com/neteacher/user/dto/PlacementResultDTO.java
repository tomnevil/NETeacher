package com.neteacher.user.dto;

import lombok.Data;

/**
 * 入学测评定级结果。
 */
@Data
public class PlacementResultDTO {
    private Integer initLevel;  // 初始级别 L1-L6
    private Integer score;      // 百分制得分
    private Integer total;      // 题目总数
    private Integer correct;    // 答对题数
    private String band;        // 级别名称
}
