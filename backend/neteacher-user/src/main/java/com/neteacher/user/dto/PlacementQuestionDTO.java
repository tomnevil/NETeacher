package com.neteacher.user.dto;

import lombok.Data;
import java.util.List;

/**
 * 入学测评定级题目（不含答案）。
 */
@Data
public class PlacementQuestionDTO {
    private String id;
    private String type;       // VOCAB / LISTENING
    private String prompt;     // 题面
    private String audioHint;  // 音标 / 可朗读文本（mock 听力）
    private List<String> options;
    private Integer level;     // 该题目所属的期望级别 L1-L6
}
