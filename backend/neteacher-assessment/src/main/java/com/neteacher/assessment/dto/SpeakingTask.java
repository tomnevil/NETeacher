package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.List;

@Data
public class SpeakingTask {
    private Long id;
    private String refText;
    private String translation;
    private String tip;
    private List<String> words;
    private int level;
}
