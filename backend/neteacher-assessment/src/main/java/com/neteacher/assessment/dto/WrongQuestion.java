package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.List;

/**
 * 错题（测评中答错的题目）。
 */
@Data
public class WrongQuestion {
    private Long questionId;
    private String subject;
    private Integer level;
    private String stem;
    private List<String> options;
    private String answer;
    private String userAnswer;
    private String explanation;
}
