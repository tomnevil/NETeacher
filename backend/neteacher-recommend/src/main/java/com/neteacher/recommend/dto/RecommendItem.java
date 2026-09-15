package com.neteacher.recommend.dto;

import lombok.Data;

/**
 * 单个推荐项。
 */
@Data
public class RecommendItem {

    private Long courseId;

    private String title;

    private String category;

    private Integer level;

    /** 推荐理由 */
    private String reason;
}
