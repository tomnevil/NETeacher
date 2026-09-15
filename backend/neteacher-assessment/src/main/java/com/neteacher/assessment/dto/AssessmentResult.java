package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.List;

/**
 * 测评结果（含自动判分与 AI 评语）。
 */
@Data
public class AssessmentResult {

    private Long id;

    private Integer level;

    private String subject;

    private String type;

    private Integer score;

    private Integer totalScore;

    private Integer correctCount;

    private Integer totalCount;

    /** LLM 生成的简短评语（无 key 时降级为 mock） */
    private String comment;

    /** 逐题明细 JSON */
    private String detail;

    /** 错题列表（仅含答错的题，含题干/选项/正确答案/解析） */
    private List<WrongQuestion> wrongQuestions;
}
