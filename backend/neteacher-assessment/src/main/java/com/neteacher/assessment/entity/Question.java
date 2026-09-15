package com.neteacher.assessment.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 题库（DE-008）。按等级 L1-L6 与学科（听/说/读/写/词/法）组织。
 *
 * 字段说明：
 * - options：选项 JSON 数组字符串，如 ["A. ...","B. ..."]（测评判分与前端共用此格式）
 * - usage：适用场景，竖线分隔，如 "practice|unit_test|placement"
 * - status：draft / reviewed / published（发布后才会进入抽题与练习）
 * - source：teacher / ai / imported / seeded（内容来源，便于追溯）
 */
@Getter
@Setter
@Entity
@Table(name = "question")
public class Question extends BaseEntity {

    private Integer level;

    /** listening / speaking / reading / writing / word / grammar */
    private String subject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stem;

    /** 选项 JSON 数组，如 ["A. ...","B. ..."] */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String options;

    /** 正确答案，如 "B" */
    private String answer;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String analysis;

    /** 题型：mcq / fill / cloze / truefalse ... */
    private String type = "mcq";

    /** 知识点标签，如 "一般过去时"，用于组卷与弱项分析 */
    private String knowledgePoint;

    /** 媒体资源地址（听力音频 / 配图），可为空 */
    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    /** 适用场景，竖线分隔：practice|unit_test|placement */
    private String usage;

    /** 状态：draft / reviewed / published */
    private String status = "published";

    /** 来源：teacher / ai / imported / seeded */
    private String source = "teacher";
}
