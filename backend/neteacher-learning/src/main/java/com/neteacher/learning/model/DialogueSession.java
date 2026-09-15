package com.neteacher.learning.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存中的对话会话状态。单实例运行足够；重启即失效（对话为短时交互）。
 */
@Data
public class DialogueSession {

    private String sessionId;

    private Long userId;

    private int grade;

    private String unit;

    private long startedAt;

    private List<DialogueMessage> messages = new ArrayList<>();

    private List<Integer> turnScores = new ArrayList<>();
}
