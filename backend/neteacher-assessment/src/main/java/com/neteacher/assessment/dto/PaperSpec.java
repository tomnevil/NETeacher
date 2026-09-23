package com.neteacher.assessment.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 组卷请求：按「等级 + 场景 + 学科配比 + 知识点」抽题。
 */
@Data
public class PaperSpec {

    private String title;

    /** 等级 L1-L6 */
    private Integer level = 1;

    /** 适用场景，与 Question.usage 对应（竖线分隔标签之一） */
    private String usage = "unit_test";

    /**
     * 学科配比。为空时退化为「在该等级已发布题目中随机取 10 题」。
     */
    private List<PaperItem> items = new ArrayList<>();

    /** 限定知识点；为空表示不限 */
    private List<String> knowledgePoints = new ArrayList<>();

    @Data
    public static class PaperItem {
        /** 学科：listening / speaking / reading / writing / word / grammar */
        private String subject;
        /** 该学科题量 */
        private int count = 5;
    }
}
