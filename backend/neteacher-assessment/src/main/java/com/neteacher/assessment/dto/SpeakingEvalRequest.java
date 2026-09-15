package com.neteacher.assessment.dto;

import lombok.Data;

@Data
public class SpeakingEvalRequest {
    private Long taskId;
    private String refText;
    private String transcript;
}
