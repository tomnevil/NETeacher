package com.neteacher.common.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 口语评测响应（音素级）。
 */
@Data
public class SpeechScoreResponse {

    /** 综合分 0-100 */
    private double overall;
    /** 准确度（音素级） */
    private double accuracy;
    /** 流利度（语速/停顿） */
    private double fluency;
    /** 完整度 */
    private double integrity;
    /** 音素明细 */
    private List<PhonemeScore> phonemes;
    /** 详细报告地址 */
    private String reportUrl;

    @Data
    public static class PhonemeScore {
        private String phoneme;
        private double score;
    }
}
