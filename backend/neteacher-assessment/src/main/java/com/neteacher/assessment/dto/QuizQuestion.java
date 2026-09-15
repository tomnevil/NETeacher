package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.List;

/**
 * 下发到前端的题目（不暴露答案）。
 */
@Data
public class QuizQuestion {

    private Long id;

    private Integer level;

    private String subject;

    private String type;

    private String stem;

    private List<String> options;
}
