package com.neteacher.common.ai;

import lombok.Data;

/**
 * AI 出题请求条件。
 *
 * <p>与具体实现解耦：调用方只描述「要什么样的题」，由 {@link QuestionGeneratePort}
 * 的适配器决定用大模型还是本地模板来产出。</p>
 */
@Data
public class QuestionGenRequest {

    /** 等级 L1-L6 */
    private Integer level = 1;

    /** 学科：listening / speaking / reading / writing / word / grammar */
    private String subject = "word";

    /** 知识点标签，如「一般过去时」，可为空（由模型自行拟定） */
    private String knowledgePoint;

    /** 适用场景，可竖线分隔：practice|unit_test|placement */
    private String usage = "practice";

    /** 题型：mcq / fill / cloze / truefalse ... */
    private String type = "mcq";

    /** 生成数量，最终由服务端收敛到 1..20 */
    private Integer count = 5;
}
