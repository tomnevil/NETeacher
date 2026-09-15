package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 学生一轮发言（已转写为文本）提交给系统，系统返回下一句追问 + 本轮评分。
 */
@Data
public class DialogueTurnRequest {

    /** 会话 ID，由 start 接口返回。 */
    private String sessionId;

    /** 学生本轮发言的转写文本。 */
    private String transcript;

    /** 本轮发言时长（秒），可不传。 */
    private Integer durationSec;
}
