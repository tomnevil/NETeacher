package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 对话发起结果：系统首条开场白。
 */
@Data
public class DialogueStartResult {

    private String sessionId;

    /** 系统开场白。 */
    private String opening;

    private Integer grade;

    private String unit;
}
