package com.neteacher.ops.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 题库题目视图（含答案，仅教师/管理员可见） */
@Data
public class QuestionDTO {
    private Long id;
    private Integer level;
    private String subject;
    private String type;
    private String stem;
    private List<String> options;
    private String answer;
    private String analysis;
    private String knowledgePoint;
    private String mediaUrl;
    /** 适用场景，竖线分隔 */
    private String usage;
    /** draft / reviewed / published */
    private String status;
    /** teacher / ai / imported / seeded */
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
