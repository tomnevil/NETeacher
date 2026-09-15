package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 发起一段 AI 口语对话的请求。系统会基于年级与当前学习单元发起话题，调动孩子开口。
 */
@Data
public class DialogueStartRequest {

    /** 年级；不传则使用账号年级，仍为空时降级为 3 年级。 */
    private Integer grade;

    /** 当前学习单元 / 场景主题，如「L2 日常会话-购物」。不传使用通用主题。 */
    private String unit;
}
