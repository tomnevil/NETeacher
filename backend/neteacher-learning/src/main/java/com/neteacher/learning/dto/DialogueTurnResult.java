package com.neteacher.learning.dto;

import lombok.Data;

/**
 * 一轮对话结果：系统追问 + 本轮发言评分（即时小分）。
 */
@Data
public class DialogueTurnResult {

    /** 系统下一句追问。 */
    private String reply;

    /** 本轮总评（0-100）。 */
    private int score;

    /** 流利度（0-100）。 */
    private int fluency;

    /** 准确度/语法词汇（0-100）。 */
    private int accuracy;

    /** 内容相关性/是否回应话题（0-100）。 */
    private int relevance;

    /** 本轮点评（中文，鼓励性）。 */
    private String comment;

    /** 第几轮（从 1 开始）。 */
    private int turn;
}
