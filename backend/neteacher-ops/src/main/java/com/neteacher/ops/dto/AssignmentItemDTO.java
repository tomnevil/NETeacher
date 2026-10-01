package com.neteacher.ops.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业列表项（学生端视角）：附带该学生自己的完成情况与逾期状态。
 */
@Data
public class AssignmentItemDTO {

    private Long id;
    private String title;
    private Long paperId;
    private Long classId;
    private String className;

    /** 截止时间，可为空 */
    private LocalDateTime dueAt;
    private Integer status;
    private LocalDateTime createdAt;

    /** 当前学生是否已提交 */
    private boolean finished;
    /** 当前学生得分（未提交为空） */
    private Integer score;

    /** 已过截止时间且仍未提交 */
    private boolean overdue;
    /** 未完成，且将在 24 小时内截止 */
    private boolean dueSoon;
}
