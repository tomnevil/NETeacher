package com.neteacher.ops.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 作业（FR-TRK-010）。以试卷 {@code paperId} 为载体，下发给某个班级 {@code classId}。
 *
 * <p>学生提交测评时携带 {@code assignmentId}，即可据此统计完成率、均分与薄弱知识点。</p>
 */
@Getter
@Setter
@Entity
@Table(name = "assignment")
public class Assignment extends BaseEntity {

    /** 作业使用的试卷 id */
    private Long paperId;

    /** 下发班级（class_group.id） */
    private Long classId;

    /** 布置人（教师 uid） */
    private Long teacherId;

    /** 作业标题 */
    private String title;

    /** 截止时间，可为空 */
    private LocalDateTime dueAt;

    /** 状态：1 正常 / 0 停用 */
    private Integer status = 1;
}
