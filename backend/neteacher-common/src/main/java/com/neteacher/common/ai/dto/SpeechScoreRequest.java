package com.neteacher.common.ai.dto;

import lombok.Data;

/**
 * 口语评测请求。
 */
@Data
public class SpeechScoreRequest {

    /** 参考文本（标准句/词） */
    private String refText;

    /** 用户录音在 COS 的地址 */
    private String audioUrl;

    /** 1=单词 2=句子 3=段落 */
    private Integer mode;
}
