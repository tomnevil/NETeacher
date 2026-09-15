package com.neteacher.assessment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交测评请求。
 */
@Data
public class QuizSubmitRequest {

    private Integer level;

    private String subject;

    /** quiz / unit / stage */
    private String type = "quiz";

    @NotNull(message = "答题不能为空")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "题目 id 不能为空")
        private Long questionId;
        private String answer;
    }
}
