package com.neteacher.server.ai;

import com.neteacher.common.ai.QuestionDraft;
import com.neteacher.common.ai.QuestionGenRequest;
import com.neteacher.common.ai.QuestionGeneratePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 出题降级实现（本地模板）。provider=mock 或缺少 apiKey 时启用，保证出题链路在无大模型密钥时依然可用。
 *
 * <p>内置少量按学科分类的模板题，按请求学科取用并在不足时循环复用，因此同一条件下
 * 生成的草稿可能重复。真实内容生产应配置 {@code ai.llm.api-key} 走 {@link LlmQuestionGenerateAdapter}。</p>
 */
public class MockQuestionGenerateAdapter implements QuestionGeneratePort {

    private record Seed(String subject, String stem, String[] options, String answer, String analysis, String kp) {
    }

    private static final List<Seed> POOL = List.of(
            // word 词汇
            new Seed("word", "What does the word \"bright\" mean in \"The room is very bright\"?",
                    new String[]{"A. 明亮的", "B. 聪明的", "C. 干净的", "D. 安静的"},
                    "A", "bright 形容光线时意为「明亮的」；形容人时可表示「聪明的」，此处搭配 room 取前者。", "形容词词义辨析"),
            new Seed("word", "Which word is a kind of fruit?",
                    new String[]{"A. carrot", "B. apple", "C. onion", "D. potato"},
                    "B", "apple 是水果；carrot/onion/potato 均为蔬菜。", "名词分类"),
            // grammar 语法
            new Seed("grammar", "Choose the correct form: She ___ to school every day.",
                    new String[]{"A. go", "B. goes", "C. going", "D. is go"},
                    "B", "主语 She 是第三人称单数，一般现在时动词需加 -s，故选 goes。", "一般现在时"),
            new Seed("grammar", "Choose the correct form: They ___ football yesterday afternoon.",
                    new String[]{"A. play", "B. plays", "C. played", "D. playing"},
                    "C", "yesterday afternoon 表示过去时间，谓语动词用一般过去时 played。", "一般过去时"),
            // reading 阅读
            new Seed("reading", "Read and choose: Tom has a little dog. Its name is Coco. It is two years old. How old is Coco?",
                    new String[]{"A. One", "B. Two", "C. Three", "D. Four"},
                    "B", "原文明确提到 It is two years old，故答案为 Two。", "细节理解"),
            new Seed("reading", "Read and choose the main idea: Lily gets up at six, has breakfast, then goes to school by bike.",
                    new String[]{"A. Lily's morning", "B. Lily's school", "C. Lily's bike", "D. Lily's breakfast"},
                    "A", "整段按时间叙述 Lily 早晨的活动，主旨是 Lily 的早晨。", "主旨大意"),
            // listening 听力
            new Seed("listening", "Listen and choose the word you hear: /kæt/",
                    new String[]{"A. cat", "B. cap", "C. cut", "D. kite"},
                    "A", "音标 /kæt/ 对应单词 cat。", "音素辨音"),
            new Seed("listening", "Listen and choose the best response to \"How are you?\"",
                    new String[]{"A. I'm fine, thank you.", "B. It's a book.", "C. Yes, I do.", "D. Good night."},
                    "A", "How are you 是问候语，常见应答为 I'm fine, thank you.", "日常交际用语"),
            // writing 写作
            new Seed("writing", "Choose the sentence with correct capitalization and punctuation.",
                    new String[]{"A. i like apples.", "B. I like apples.", "C. I like apples?", "D. i Like apples"},
                    "B", "句首单词首字母需大写，陈述句句末用句号，故选 B。", "书写规范"),
            new Seed("writing", "Which is a complete sentence?",
                    new String[]{"A. Because I was late.", "B. I was late for school.", "C. Although it rained.", "D. After school today."},
                    "B", "完整句须有主谓结构且意思完整，A/C/D 均为从句或短语。", "句子结构"),
            // speaking 口语
            new Seed("speaking", "In a shop, someone says \"Can I help you?\". Choose the best response.",
                    new String[]{"A. Yes, I'd like a pen.", "B. You're welcome.", "C. Nice to meet you.", "D. See you later."},
                    "A", "店员询问需求时，应说明想买的物品，故选 A。", "情景应答"),
            new Seed("speaking", "Which word has the same vowel sound as \"cake\"?",
                    new String[]{"A. cat", "B. name", "C. cap", "D. bag"},
                    "B", "cake 与 name 中的字母 a 均发 /eɪ/；其余发 /æ/。", "元音发音")
    );

    @Override
    public List<QuestionDraft> generate(QuestionGenRequest request) {
        int n = clamp(request.getCount() == null ? 5 : request.getCount(), 1, 20);
        String subject = request.getSubject() == null ? "" : request.getSubject().toLowerCase(Locale.ROOT);
        List<Seed> pool = POOL.stream()
                .filter(s -> s.subject().equals(subject))
                .toList();
        if (pool.isEmpty()) {
            pool = POOL;
        }
        String kp = request.getKnowledgePoint();
        List<QuestionDraft> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Seed s = pool.get(i % pool.size());
            QuestionDraft d = new QuestionDraft();
            d.setStem(s.stem());
            d.setOptions(List.of(s.options()));
            d.setAnswer(s.answer());
            d.setAnalysis(s.analysis());
            d.setKnowledgePoint((kp == null || kp.isBlank()) ? s.kp() : kp);
            out.add(d);
        }
        return out;
    }

    @Override
    public String provider() {
        return "mock";
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
