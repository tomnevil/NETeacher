package com.neteacher.ops.dto;

import lombok.Data;

/**
 * 题库覆盖度统计。
 *
 * <p>按请求维度 group by：默认（by=subject）统计「等级 × 学科」；
 * by=knowledgePoint 时统计「等级 × 知识点」，此时 subject 为空。</p>
 */
@Data
public class CoverageDTO {
    private Integer level;
    private String subject;
    /** 按知识点维度统计时的知识点标签 */
    private String knowledgePoint;
    private long total;
    private long published;
    private long draft;
}
