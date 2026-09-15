package com.neteacher.learning.dto;

import lombok.Data;

import java.util.List;

/**
 * 结束总评：整体评分 + 优势/薄弱点 + 中文总评 + 下一步强化建议。
 */
@Data
public class DialogueEndResult {

    /** 整体评分（0-100）。 */
    private int overallScore;

    private List<String> strengths;

    private List<String> weaknesses;

    /** 中文总评。 */
    private String summary;

    /** 下一步强化练习建议（中文，2-4 条）。 */
    private List<String> suggestions;

    /** 对话轮数。 */
    private int turns;
}
