package com.neteacher.recommend.dto;

import lombok.Data;

import java.util.List;

/**
 * 由对话薄弱点生成强化练习任务的请求。
 */
@Data
public class RecommendByDialogueRequest {

    /** 年级，用于推断课程等级。 */
    private Integer grade;

    /** 对话单元/场景（仅用于说明，可选）。 */
    private String unit;

    /** 对话总评中的薄弱点列表（中文短句）。 */
    private List<String> weaknesses;
}
