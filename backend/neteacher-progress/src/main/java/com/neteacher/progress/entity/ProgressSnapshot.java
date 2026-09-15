package com.neteacher.progress.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 学习进度快照（仪表盘数据源）。
 */
@Getter
@Setter
@Entity
@Table(name = "progress_snapshot")
public class ProgressSnapshot extends BaseEntity {

    private Long userId;

    private Integer masteredWords;

    private Double speakingAvg;

    private Integer courseCompletion;

    private Integer streakDays;
}
