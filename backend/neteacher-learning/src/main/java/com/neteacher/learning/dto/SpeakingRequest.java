package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 口语/听力评测请求。transcript 为用户的语音转写文本（由前端 Web Speech API 或手动输入提供）。
 */
@Data
public class SpeakingRequest {
    private Long courseId;
    private String module = "speaking";
    private String targetText;
    private String transcript;
    private Integer durationSec;
}
