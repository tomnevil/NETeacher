package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.List;

@Data
public class SpeakingEvalResult {
    private int score;
    private int accuracy;
    private int fluency;
    private int integrity;
    private String feedback;
    private List<PhonemeMark> phonemes;
    private List<Integer> waveformRef;
    private List<Integer> waveformUser;

    @Data
    public static class PhonemeMark {
        private String text;
        private int score;
        private String status; // good | fair | weak
    }
}
