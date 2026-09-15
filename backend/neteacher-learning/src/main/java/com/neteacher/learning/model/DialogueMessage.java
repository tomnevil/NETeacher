package com.neteacher.learning.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息。role = system（系统/AI）或 student（学生）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogueMessage {

    private String role;

    private String content;
}
