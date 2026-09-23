package com.neteacher.assessment.config;

import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题库种子：覆盖 L1-L6 × 听/说/读/写/词/法 共 36 个「等级 × 学科」组合。
 *
 * <p>策略为「按缺口补种」：对每个（等级, 学科）组合，仅当该组合尚无题目时才插入，
 * 因此重复启动不会重复灌数据，也能为已有库补齐 L4-L6 等空白格子（铺覆盖度）。</p>
 *
 * <p>同时执行一次历史数据回填：只补齐为空的 knowledgePoint / usage / source，
 * 不覆盖已有取值；其中 source 仅在「知识点为空且来源为空或 teacher」时才纠正为 seeded，
 * 以免误改教师手工创建（teacher）或 CSV 导入（imported）的题目。</p>
 */
@Component
@RequiredArgsConstructor
public class QuestionSeeder implements CommandLineRunner {

    private static final String DEFAULT_USAGE = "practice|unit_test";

    private final QuestionRepository questionRepo;

    private record Seed(int level, String subject, String stem, String options, String answer,
                        String analysis, String knowledgePoint) {
    }

    private static final List<Seed> SEEDS = List.of(
            // ---------------- L1 ----------------
            new Seed(1, "listening", "听音选词：/kæt/",
                    "[\"A. cat\",\"B. cut\",\"C. kite\"]", "A", "字母 a 在闭音节中发 /æ/。", "音素辨音"),
            new Seed(1, "speaking", "早上见到老师，应说",
                    "[\"A. Good morning.\",\"B. Good night.\",\"C. Goodbye.\"]", "A", "上午见面用 Good morning 问候。", "日常问候"),
            new Seed(1, "reading", "Tom is a boy. He is seven. How old is Tom?",
                    "[\"A. Six\",\"B. Seven\",\"C. Eight\"]", "B", "原文提到 He is seven。", "细节理解"),
            new Seed(1, "writing", "“I ___ a student.” 应填",
                    "[\"A. am\",\"B. is\",\"C. are\"]", "A", "第一人称 I 搭配 am。", "be 动词"),
            new Seed(1, "word", "选出“苹果”的英文",
                    "[\"A. apple\",\"B. banana\",\"C. orange\"]", "A", "apple 意为苹果。", "名词词义"),
            new Seed(1, "grammar", "“This ___ a book.” 应填",
                    "[\"A. is\",\"B. are\",\"C. am\"]", "A", "单数名词搭配 is。", "be 动词"),

            // ---------------- L2 ----------------
            new Seed(2, "listening", "听音选词：/dɒɡ/",
                    "[\"A. dog\",\"B. dug\",\"C. bag\"]", "A", "o 在重读闭音节中发 /ɒ/。", "音素辨音"),
            new Seed(2, "speaking", "想知道对方的名字，应该问",
                    "[\"A. What's your name?\",\"B. How are you?\",\"C. How old are you?\"]", "A",
                    "询问姓名用 What's your name?。", "情景问答"),
            new Seed(2, "reading", "“apple” 的复数形式是",
                    "[\"A. apple\",\"B. apples\",\"C. applees\"]", "B", "一般名词直接加 -s 构成复数。", "名词复数"),
            new Seed(2, "writing", "“He ___ ten years old.” 应填",
                    "[\"A. am\",\"B. is\",\"C. are\"]", "B", "第三人称单数用 is。", "be 动词"),
            new Seed(2, "word", "“cat” 的中文意思是",
                    "[\"A. 猫\",\"B. 狗\",\"C. 鸟\"]", "A", "cat 意为猫。", "名词词义"),
            new Seed(2, "grammar", "“There ___ two books on the desk.” 应填",
                    "[\"A. is\",\"B. are\",\"C. am\"]", "B", "复数名词用 are。", "There be 句型"),

            // ---------------- L3 ----------------
            new Seed(3, "listening", "听音选词：/ʃiːp/",
                    "[\"A. ship\",\"B. sheep\",\"C. cheap\"]", "B", "ee 组合发长音 /iː/。", "元音辨音"),
            new Seed(3, "speaking", "想请对方重复一遍，应说",
                    "[\"A. Pardon?\",\"B. Stop.\",\"C. No.\"]", "A", "Pardon? 是礼貌地请求重复。", "交际用语"),
            new Seed(3, "reading", "“book” 的同类词是",
                    "[\"A. pen\",\"B. run\",\"C. red\"]", "A", "pen 与 book 同属文具类名词。", "词义归类"),
            new Seed(3, "writing", "下列句子标点书写正确的是",
                    "[\"A. i like apples.\",\"B. I like apples.\",\"C. I like apples?\"]", "B",
                    "句首大写、陈述句以句号结尾。", "书写规范"),
            new Seed(3, "word", "选出拼写正确的单词",
                    "[\"A. beautiful\",\"B. betiful\",\"C. beutiful\"]", "A",
                    "beautiful 正确拼写为 b-e-a-u-t-i-f-u-l。", "单词拼写"),
            new Seed(3, "grammar", "“She ___ to school every day.” 应填",
                    "[\"A. go\",\"B. goes\",\"C. going\"]", "B", "第三人称单数的一般现在时动词加 -s。", "一般现在时"),

            // ---------------- L4 ----------------
            new Seed(4, "listening", "听对话：Where does Amy go on Sunday?",
                    "[\"A. She goes to the park.\",\"B. It is Sunday.\",\"C. Yes, she does.\"]", "A",
                    "问句询问地点，应答需给出地点信息。", "对话理解"),
            new Seed(4, "speaking", "店员说 “Can I help you?”，最合适的回答是",
                    "[\"A. Yes, I'd like a pen.\",\"B. You're welcome.\",\"C. See you.\"]", "A",
                    "应说明自己想买的物品。", "情景应答"),
            new Seed(4, "reading", "Mary gets up at six, has breakfast, then goes to school by bus. 主旨是",
                    "[\"A. Mary's morning\",\"B. Mary's bus\",\"C. Mary's school\"]", "A",
                    "全段按时间叙述 Mary 早晨的活动。", "主旨大意"),
            new Seed(4, "writing", "下列哪一项是完整句",
                    "[\"A. Because I was late.\",\"B. I was late for school.\",\"C. Although it rained.\"]", "B",
                    "完整句需有主谓结构且意思完整。", "句子结构"),
            new Seed(4, "word", "选出与 big 意思相反的词",
                    "[\"A. small\",\"B. tall\",\"C. long\"]", "A", "big 的反义词是 small。", "反义词"),
            new Seed(4, "grammar", "“They ___ football yesterday afternoon.” 应填",
                    "[\"A. play\",\"B. plays\",\"C. played\"]", "C", "yesterday afternoon 表示过去，用一般过去时。", "一般过去时"),

            // ---------------- L5 ----------------
            new Seed(5, "listening", "听短文：The boy is looking for his ___.",
                    "[\"A. bag\",\"B. desk\",\"C. teacher\"]", "A",
                    "looking for 表示寻找，常搭配丢失的物品。", "短文理解"),
            new Seed(5, "speaking", "想邀请同学一起踢球，应说",
                    "[\"A. Let's play football together.\",\"B. I play football.\",\"C. Football is good.\"]", "A",
                    "Let's ... 是提出邀请的常用句式。", "邀请表达"),
            new Seed(5, "reading", "It is raining heavily, so the match is ___.（推断）",
                    "[\"A. put off\",\"B. started\",\"C. finished\"]", "A",
                    "由因果连词 so 与天气情况可推断比赛被推迟。", "推理判断"),
            new Seed(5, "writing", "连词成句：school / I / to / go / by / bus",
                    "[\"A. I go to school by bus.\",\"B. I by bus go to school.\",\"C. Go to school I by bus.\"]", "A",
                    "英语基本语序为主语 + 谓语 + 宾语 + 方式状语。", "语序"),
            new Seed(5, "word", "“careful” 的副词形式是",
                    "[\"A. carefully\",\"B. carefullly\",\"C. carefuly\"]", "A",
                    "形容词变副词一般在词尾加 -ly。", "构词法"),
            new Seed(5, "grammar", "“Look! The children ___ in the park.” 应填",
                    "[\"A. play\",\"B. are playing\",\"C. played\"]", "B",
                    "Look! 提示动作正在发生，用现在进行时。", "现在进行时"),

            // ---------------- L6 ----------------
            new Seed(6, "listening", "听短文，选择最佳标题",
                    "[\"A. A Day at School\",\"B. My Cat\",\"C. The Weather\"]", "A",
                    "短文围绕一天的校园生活展开。", "主旨概括"),
            new Seed(6, "speaking", "表达观点：I think reading is ___.",
                    "[\"A. useful\",\"B. a book\",\"C. read\"]", "A",
                    "be 动词后接形容词作表语，说明看法。", "观点表达"),
            new Seed(6, "reading", "作者认为阅读很有用，其语气是",
                    "[\"A. positive\",\"B. negative\",\"C. unsure\"]", "A",
                    "“有用”属于正面评价，语气积极。", "作者态度"),
            new Seed(6, "writing", "把 He likes apples. 改为一般疑问句",
                    "[\"A. Does he like apples?\",\"B. Likes he apples?\",\"C. Do he like apples?\"]", "A",
                    "第三人称单数的一般现在时用 Does 开头，动词还原。", "句式转换"),
            new Seed(6, "word", "选出与 happy 意思最接近的词",
                    "[\"A. glad\",\"B. sad\",\"C. angry\"]", "A", "glad 与 happy 都表示高兴。", "近义词"),
            new Seed(6, "grammar", "“If it ___ tomorrow, we will stay at home.” 应填",
                    "[\"A. rains\",\"B. will rain\",\"C. rained\"]", "A",
                    "条件状语从句遵循主将从现，从句用一般现在时。", "条件状语从句")
    );

    @Override
    public void run(String... args) {
        backfill();
        seedMissingCombinations();
    }

    /** 回填历史数据：只补齐空字段，不覆盖已有取值 */
    private void backfill() {
        List<Question> dirty = new ArrayList<>();
        for (Question q : questionRepo.findAll()) {
            boolean changed = false;
            boolean kpBlank = isBlank(q.getKnowledgePoint());
            if (kpBlank) {
                q.setKnowledgePoint(defaultKnowledgePoint(q.getSubject()));
                changed = true;
            }
            if (isBlank(q.getUsage())) {
                q.setUsage(DEFAULT_USAGE);
                changed = true;
            }
            // 旧种子从未设置来源（落库为实体默认值 teacher），以「知识点为空 + 来源为空或 teacher」识别并纠正
            if (kpBlank && (isBlank(q.getSource()) || "teacher".equals(q.getSource()))) {
                q.setSource("seeded");
                changed = true;
            }
            if (changed) {
                dirty.add(q);
            }
        }
        if (!dirty.isEmpty()) {
            questionRepo.saveAll(dirty);
        }
    }

    /** 按「等级 × 学科」缺口补种，保证覆盖度且不重复灌数据 */
    private void seedMissingCombinations() {
        Map<String, List<Seed>> grouped = SEEDS.stream()
                .collect(Collectors.groupingBy(s -> s.level() + "|" + s.subject(),
                        LinkedHashMap::new, Collectors.toList()));
        List<Question> toSave = new ArrayList<>();
        for (Map.Entry<String, List<Seed>> entry : grouped.entrySet()) {
            Seed first = entry.getValue().get(0);
            if (questionRepo.countByLevelAndSubject(first.level(), first.subject()) > 0) {
                continue;
            }
            for (Seed s : entry.getValue()) {
                toSave.add(toQuestion(s));
            }
        }
        if (!toSave.isEmpty()) {
            questionRepo.saveAll(toSave);
        }
    }

    private Question toQuestion(Seed s) {
        Question q = new Question();
        q.setLevel(s.level());
        q.setSubject(s.subject());
        q.setType("mcq");
        q.setStem(s.stem());
        q.setOptions(s.options());
        q.setAnswer(s.answer());
        q.setAnalysis(s.analysis());
        q.setKnowledgePoint(s.knowledgePoint());
        q.setUsage(DEFAULT_USAGE);
        // 种子题默认发布：可直接进入抽题与练习
        q.setStatus("published");
        q.setSource("seeded");
        return q;
    }

    private static String defaultKnowledgePoint(String subject) {
        if (subject == null) {
            return "综合";
        }
        switch (subject) {
            case "listening":
                return "听力理解";
            case "speaking":
                return "情景交际";
            case "reading":
                return "阅读理解";
            case "writing":
                return "书面表达";
            case "word":
                return "词汇积累";
            case "grammar":
                return "语法运用";
            default:
                return "综合";
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
