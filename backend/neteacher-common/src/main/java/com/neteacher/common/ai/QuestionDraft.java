package com.neteacher.common.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 生成的单条题目草稿（尚未落库）。
 *
 * <p>只承载「内容」相关字段；等级、学科、场景等分发条件来自 {@link QuestionGenRequest}，
 * 由落库侧回填到 Question 实体。</p>
 */
@Data
public class QuestionDraft {

    /** 题干 */
    private String stem;

    /** 选项列表，如 ["A. go", "B. goes"] */
    private List<String> options = new ArrayList<>();

    /** 正确答案，如 "B" */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 知识点（可为空，此时落库侧使用请求中的知识点） */
    private String knowledgePoint;
}
