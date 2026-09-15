package com.neteacher.ops.dto;

import lombok.Data;

import java.util.List;

/** 题库题目创建/更新入参 */
@Data
public class QuestionUpsertDTO {
    private Integer level;
    /** listening / speaking / reading / writing / word / grammar */
    private String subject;
    private String type;
    private String stem;
    /** 选项列表，后端会序列化为 JSON 存储 */
    private List<String> options;
    private String answer;
    private String analysis;
    private String knowledgePoint;
    private String mediaUrl;
    /** 适用场景，竖线分隔，如 practice|unit_test */
    private String usage;
    /** draft / reviewed / published */
    private String status;
    /** teacher / ai / imported / seeded */
    private String source;
}
