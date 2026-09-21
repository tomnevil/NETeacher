package com.neteacher.server.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.common.ai.QuestionDraft;
import com.neteacher.common.ai.QuestionGenRequest;
import com.neteacher.common.ai.QuestionGeneratePort;

import java.util.List;
import java.util.Locale;

/**
 * 基于大模型的出题适配器。复用 {@link LlmPort}（屏蔽厂商差异），把返回结果解析为结构化草稿。
 *
 * <p>解析失败时返回空列表而不是抛错，调用方据此生成 0 条草稿；这样在模型返回不稳定时
 * 不会让整个题库功能不可用。</p>
 */
public class LlmQuestionGenerateAdapter implements QuestionGeneratePort {

    private final LlmPort llm;
    private final ObjectMapper lenient;

    public LlmQuestionGenerateAdapter(LlmPort llm, ObjectMapper objectMapper) {
        this.llm = llm;
        this.lenient = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<QuestionDraft> generate(QuestionGenRequest request) {
        String raw = llm.chat(buildPrompt(request));
        return parse(raw);
    }

    @Override
    public String provider() {
        return llm.vendor();
    }

    private String buildPrompt(QuestionGenRequest r) {
        int n = clamp(r.getCount() == null ? 5 : r.getCount(), 1, 20);
        String subject = r.getSubject() == null ? "word" : r.getSubject();
        String type = r.getType() == null ? "mcq" : r.getType();
        String kp = (r.getKnowledgePoint() == null || r.getKnowledgePoint().isBlank())
                ? "由你自行拟定一个适合该等级的常见知识点" : "限定知识点：" + r.getKnowledgePoint();
        return String.format(Locale.ROOT, ""
                        + "你是中小学英语出题老师。请生成 %d 道英语练习题，要求：\n"
                        + "1) 等级 L%s（L1 最简单，L6 最难）；\n"
                        + "2) 学科：%s；\n"
                        + "3) 题型：%s；\n"
                        + "4) %s；\n"
                        + "5) 每题提供 4 个选项，并给出正确答案与中文简要解析。\n"
                        + "只输出如下 JSON 数组，不要包含任何解释性文字或代码块标记：\n"
                        + "[{\"stem\":\"题干\",\"options\":[\"A. ...\",\"B. ...\",\"C. ...\",\"D. ...\"],"
                        + "\"answer\":\"B\",\"analysis\":\"解析\",\"knowledgePoint\":\"知识点\"}]",
                n, r.getLevel(), subject, type, kp);
    }

    private List<QuestionDraft> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String text = raw.trim();
        // 容错：模型可能用 ```json ... ``` 包裹，或直接夹杂前后缀说明
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return List.of();
        }
        try {
            List<QuestionDraft> drafts = lenient.readValue(text.substring(start, end + 1),
                    new TypeReference<List<QuestionDraft>>() {
                    });
            return drafts == null ? List.of() : drafts;
        } catch (Exception e) {
            return List.of();
        }
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
