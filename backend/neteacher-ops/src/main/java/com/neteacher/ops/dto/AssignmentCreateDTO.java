package com.neteacher.ops.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 布置作业请求。 */
@Data
public class AssignmentCreateDTO {

    /** 试卷 id */
    private Long paperId;

    /** 班级 id */
    private Long classId;

    /** 作业标题，为空时自动用试卷标题 */
    private String title;

    /** 截止时间（可为空） */
    private LocalDateTime dueAt;
}
