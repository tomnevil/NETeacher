package com.neteacher.ops.dto;

import lombok.Data;

/** 题库覆盖度统计（按 等级 × 学科） */
@Data
public class CoverageDTO {
    private Integer level;
    private String subject;
    private long total;
    private long published;
    private long draft;
}
