package com.neteacher.learning.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 学习行为记录（单词/语法/口语/听力/对话五模块，DE-011）。
 */
@Getter
@Setter
@Entity
@Table(name = "learning_record")
public class LearningRecord extends BaseEntity {

    private Long userId;

    private Long courseId;

    private Long lessonId;

    /** word / grammar / speaking / listening / dialogue */
    @Column(length = 16)
    private String module;

    private Integer score;

    private Integer durationSec;

    private Boolean finished = false;

    /** 明细（JSON 字符串，记录错题/答题轨迹等） */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String detail;
}
