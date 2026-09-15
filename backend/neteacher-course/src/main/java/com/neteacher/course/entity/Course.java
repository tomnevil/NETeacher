package com.neteacher.course.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程（DE-005）。level 对应 L1-L6，category 区分主修/拓展/专项。
 */
@Getter
@Setter
@Entity
@Table(name = "course")
public class Course extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 512)
    private String cover;

    /** L1-L6 难度等级 */
    private Integer level;

    /** 适用年级（1~12）；为空表示全年级通用 */
    private Integer grade;

    /** MAJOR 主修 / EXTENSION 拓展 / SPECIAL 专项 */
    @Column(length = 16)
    private String category;

    /** 专项子类型：WORD 单词 / SPEAKING 口语 / LISTENING 听力 / READING 阅读 / GRAMMAR 语法；非专项为空 */
    @Column(length = 16)
    private String topic;

    /** 总时长（秒） */
    private Integer durationSec;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 课时数 */
    private Integer lessonCount;

    /** 标签，逗号分隔，如 "口语,听力" */
    @Column(length = 255)
    private String tags;
}
