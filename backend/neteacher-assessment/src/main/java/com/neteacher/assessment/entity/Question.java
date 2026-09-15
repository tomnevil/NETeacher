package com.neteacher.assessment.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 题库（DE-008）。按等级 L1-L6 与学科（听/说/读/写）组织。
 */
@Getter
@Setter
@Entity
@Table(name = "question")
public class Question extends BaseEntity {

    private Integer level;

    /** listening / speaking / reading / writing */
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

    private String type = "mcq";
}
