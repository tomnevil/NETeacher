package com.neteacher.recommend.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 个性化学习路径。recommendItems 为推荐内容 JSON（课程/专项包），subjectScores 为各科学情。
 */
@Getter
@Setter
@Entity
@Table(name = "learning_path")
public class LearningPath extends BaseEntity {

    private Long userId;

    /** 当前级别 L1-L6 */
    private Integer currentLevel;

    /** 下一目标级别 */
    private Integer nextLevel;

    /** 推荐内容（JSON） */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String recommendItems;

    /** 各科学情 JSON：{subject: score} */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String subjectScores;
}
