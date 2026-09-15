package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 结束对话并生成总评的请求。
 */
@Data
public class DialogueEndRequest {

    /** 会话 ID。 */
    private String sessionId;
}
