package com.neteacher.learning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交学习记录请求。
 */
@Data
public class LearningRecordRequest {

    @NotNull(message = "课程 id 不能为空")
    private Long courseId;

    private Long lessonId;

    /** word / grammar / speaking / listening / dialogue */
    private String module;

    private Integer score;

    private Integer durationSec;

    private Boolean finished = false;

    /** 明细 JSON 字符串 */
    private String detail;
}
