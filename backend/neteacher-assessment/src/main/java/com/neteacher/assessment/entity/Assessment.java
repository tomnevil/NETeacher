package com.neteacher.assessment.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 评测记录（DE-009 单元测 / DE-010 阶段测）。unit=单元测，stage=阶段测，quiz=随堂测。
 */
@Getter
@Setter
@Entity
@Table(name = "assessment")
public class Assessment extends BaseEntity {

    private Long userId;

    /** quiz / unit / stage */
    private String type;

    /** listening / speaking / reading / writing */
    private String subject;

    private Integer level;

    private Integer score;

    private Integer totalScore;

    private Boolean finished = false;

    /** 逐题明细 JSON */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String detail;
}
