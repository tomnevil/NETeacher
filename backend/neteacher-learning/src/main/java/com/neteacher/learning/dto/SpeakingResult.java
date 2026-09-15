package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 口语/听力评测结果。
 */
@Data
public class SpeakingResult {
    private int score;
    private String feedback;
    private String transcript;
    private String targetText;
}
