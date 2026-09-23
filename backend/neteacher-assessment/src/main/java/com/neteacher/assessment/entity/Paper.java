package com.neteacher.assessment.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 试卷（P3 组卷）。按「等级 + 场景 + 学科配比 + 知识点」从题库抽取并固化的题目集合，
 * 便于教师复用与跟踪，也为后续作业布置（FR-TRK-010）提供载体。
 */
@Getter
@Setter
@Entity
@Table(name = "paper")
public class Paper extends BaseEntity {

    private String title;

    private Integer level;

    /** 适用场景：practice / unit_test / placement */
    private String usage;

    /** 出题人（教师 uid） */
    private Long createdBy;

    /** 题目 id 列表（JSON 数组字符串），保持抽题顺序 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String questionIds;

    /** 组卷时限定的知识点（JSON 数组字符串），为空表示不限 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String knowledgePoints;
}
