package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 组卷结果：固化后的试卷 + 题目明细 + 未凑齐的缺口说明。 */
@Data
public class PaperDTO {

    private Long id;
    private String title;
    private Integer level;
    private String usage;
    private Long createdBy;

    private List<Long> questionIds = new ArrayList<>();
    private List<QuizQuestion> questions = new ArrayList<>();

    /** 未能凑齐的学科与缺口数量，如 "word 缺 3 题" */
    private List<String> shortfalls = new ArrayList<>();
}
