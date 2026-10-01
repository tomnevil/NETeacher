package com.neteacher.assessment.config;

import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库种子：覆盖 L1-L6 × 听/说/读/写/词/法 共 36 个「等级 × 学科」组合，每个组合 3 题。
 *
 * <p>学段划分：L1-L4 对应<b>小学</b>（一年级入门 → 五六年级），L5-L6 对应<b>初中</b>
 * （初一初二 → 初三），难度与知识点随等级递进。</p>
 *
 * <p>策略为「按缺口补到目标数 + 题干去重」：对每个（等级, 学科）组合，统计已有题目数，
 * 不足 {@link #TARGET_PER_CELL} 时才从种子表补足；已存在的题干（stem）会被跳过。
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

    /** 每个「等级 × 学科」的目标题量 */
    private static final int TARGET_PER_CELL = 3;

    private final QuestionRepository questionRepo;

    private record Seed(int level, String subject, String stem, String options, String answer,
                        String analysis, String knowledgePoint) {
    }

    private static final List<Seed> SEEDS = List.of(
            // ==================== L1（小学·一年级入门） ====================
            new Seed(1, "listening", "听音选词：/kæt/",
                    "[\"A. cat\",\"B. cut\",\"C. kite\"]", "A", "字母 a 在闭音节中发 /æ/。", "音素辨音"),
            new Seed(1, "listening", "听音选词：/bʌs/",
                    "[\"A. bus\",\"B. bath\",\"C. boss\"]", "A", "字母 u 在重读闭音节中常发 /ʌ/。", "音素辨音"),
            new Seed(1, "listening", "听音选词：/pen/",
                    "[\"A. pen\",\"B. pan\",\"C. pin\"]", "A", "字母 e 在闭音节中发 /e/。", "音素辨音"),
            new Seed(1, "speaking", "早上见到老师，应说",
                    "[\"A. Good morning.\",\"B. Good night.\",\"C. Goodbye.\"]", "A", "上午见面用 Good morning 问候。", "日常问候"),
            new Seed(1, "speaking", "与朋友告别时，应说",
                    "[\"A. Goodbye.\",\"B. Thank you.\",\"C. Sorry.\"]", "A", "分别时用 Goodbye 道别。", "日常问候"),
            new Seed(1, "speaking", "得到别人帮助后，应说",
                    "[\"A. Thank you.\",\"B. Hello.\",\"C. Good night.\"]", "A", "表达感谢用 Thank you。", "礼貌用语"),
            new Seed(1, "reading", "Tom is a boy. He is seven. How old is Tom?",
                    "[\"A. Six\",\"B. Seven\",\"C. Eight\"]", "B", "原文提到 He is seven。", "细节理解"),
            new Seed(1, "reading", "The cat is black. What color is the cat?",
                    "[\"A. Black\",\"B. White\",\"C. Red\"]", "A", "原文直接给出 black。", "细节理解"),
            new Seed(1, "reading", "Ann has two books. How many books does Ann have?",
                    "[\"A. Two\",\"B. Three\",\"C. Four\"]", "A", "原文提到 two books。", "细节理解"),
            new Seed(1, "writing", "“I ___ a student.” 应填",
                    "[\"A. am\",\"B. is\",\"C. are\"]", "A", "第一人称 I 搭配 am。", "be 动词"),
            new Seed(1, "writing", "“___ am Tom.” 句首应填",
                    "[\"A. I\",\"B. i\",\"C. My\"]", "A", "英语句子首字母必须大写，第一人称主格用 I。", "书写规范"),
            new Seed(1, "writing", "“This is a pen___” 句末应填",
                    "[\"A. .\",\"B. ?\",\"C. ,\"]", "A", "陈述句以句号结尾。", "标点符号"),
            new Seed(1, "word", "选出“苹果”的英文",
                    "[\"A. apple\",\"B. banana\",\"C. orange\"]", "A", "apple 意为苹果。", "名词词义"),
            new Seed(1, "word", "选出“狗”的英文",
                    "[\"A. dog\",\"B. duck\",\"C. desk\"]", "A", "dog 意为狗。", "名词词义"),
            new Seed(1, "word", "选出“书”的英文",
                    "[\"A. book\",\"B. look\",\"C. cook\"]", "A", "book 意为书。", "名词词义"),
            new Seed(1, "grammar", "“This ___ a book.” 应填",
                    "[\"A. is\",\"B. are\",\"C. am\"]", "A", "单数名词搭配 is。", "be 动词"),
            new Seed(1, "grammar", "“I ___ Tom.” 应填",
                    "[\"A. am\",\"B. is\",\"C. are\"]", "A", "第一人称 I 搭配 am。", "be 动词"),
            new Seed(1, "grammar", "“He ___ my father.” 应填",
                    "[\"A. is\",\"B. am\",\"C. are\"]", "A", "第三人称单数搭配 is。", "be 动词"),

            // ==================== L2（小学·二三年级） ====================
            new Seed(2, "listening", "听音选词：/dɒɡ/",
                    "[\"A. dog\",\"B. dug\",\"C. bag\"]", "A", "o 在重读闭音节中发 /ɒ/。", "音素辨音"),
            new Seed(2, "listening", "听音选词：/bɜːd/",
                    "[\"A. bird\",\"B. bed\",\"C. bad\"]", "A", "字母组合 ir 发 /ɜː/。", "音素辨音"),
            new Seed(2, "listening", "听音选词：/kɑː/",
                    "[\"A. car\",\"B. cat\",\"C. cut\"]", "A", "字母组合 ar 发 /ɑː/。", "音素辨音"),
            new Seed(2, "speaking", "想知道对方的名字，应该问",
                    "[\"A. What's your name?\",\"B. How are you?\",\"C. How old are you?\"]", "A",
                    "询问姓名用 What's your name?。", "情景问答"),
            new Seed(2, "speaking", "想知道对方的年龄，应该问",
                    "[\"A. How old are you?\",\"B. How are you?\",\"C. What's this?\"]", "A",
                    "询问年龄用 How old are you?。", "情景问答"),
            new Seed(2, "speaking", "问候对方的身体状况，应说",
                    "[\"A. How are you?\",\"B. Who are you?\",\"C. Where are you?\"]", "A",
                    "How are you? 用于问候近况。", "情景问答"),
            new Seed(2, "reading", "“apple” 的复数形式是",
                    "[\"A. apple\",\"B. apples\",\"C. applees\"]", "B", "一般名词直接加 -s 构成复数。", "名词复数"),
            new Seed(2, "reading", "Tom goes to school at seven. When does Tom go to school?",
                    "[\"A. At seven.\",\"B. At eight.\",\"C. At nine.\"]", "A", "原文提到 at seven。", "细节理解"),
            new Seed(2, "reading", "The bag is on the desk. Where is the bag?",
                    "[\"A. On the desk.\",\"B. Under the desk.\",\"C. In the desk.\"]", "A",
                    "原文提到 on the desk。", "细节理解"),
            new Seed(2, "writing", "“He ___ ten years old.” 应填",
                    "[\"A. am\",\"B. is\",\"C. are\"]", "B", "第三人称单数用 is。", "be 动词"),
            new Seed(2, "writing", "连词成句：am / I / Tom",
                    "[\"A. I am Tom.\",\"B. Am I Tom.\",\"C. Tom am I.\"]", "A",
                    "陈述句语序为「主语 + 谓语 + 表语」。", "语序"),
            new Seed(2, "writing", "“My name ___ Tom.” 应填",
                    "[\"A. is\",\"B. am\",\"C. are\"]", "A", "第三人称单数用 is。", "be 动词"),
            new Seed(2, "word", "“cat” 的中文意思是",
                    "[\"A. 猫\",\"B. 狗\",\"C. 鸟\"]", "A", "cat 意为猫。", "名词词义"),
            new Seed(2, "word", "“香蕉”的英文是",
                    "[\"A. banana\",\"B. band\",\"C. banner\"]", "A", "banana 意为香蕉。", "名词词义"),
            new Seed(2, "word", "“老师”的英文是",
                    "[\"A. teacher\",\"B. teach\",\"C. teaches\"]", "A", "teacher 是表示职业的名词。", "名词词义"),
            new Seed(2, "grammar", "“There ___ two books on the desk.” 应填",
                    "[\"A. is\",\"B. are\",\"C. am\"]", "B", "复数名词用 are。", "There be 句型"),
            new Seed(2, "grammar", "“I ___ like apples.”（否定）应填",
                    "[\"A. don't\",\"B. doesn't\",\"C. am not\"]", "A",
                    "第一人称的一般现在时否定用 don't。", "一般现在时"),
            new Seed(2, "grammar", "“___ you like milk?” 应填",
                    "[\"A. Do\",\"B. Does\",\"C. Are\"]", "A", "第二人称用助动词 Do 构成疑问句。", "一般疑问句"),

            // ==================== L3（小学·四年级） ====================
            new Seed(3, "listening", "听音选词：/ʃiːp/",
                    "[\"A. ship\",\"B. sheep\",\"C. cheap\"]", "B", "ee 组合发长音 /iː/。", "元音辨音"),
            new Seed(3, "listening", "听音选词：/triː/",
                    "[\"A. tree\",\"B. three\",\"C. try\"]", "A", "ee 组合发长音 /iː/，tree 读作 /triː/。", "元音辨音"),
            new Seed(3, "listening", "听音选词：/ˈhæpi/",
                    "[\"A. happy\",\"B. hobby\",\"C. hop\"]", "A", "重音在第一个音节，a 发 /æ/。", "音节辨音"),
            new Seed(3, "speaking", "想请对方重复一遍，应说",
                    "[\"A. Pardon?\",\"B. Stop.\",\"C. No.\"]", "A", "Pardon? 是礼貌地请求重复。", "交际用语"),
            new Seed(3, "speaking", "想请别人帮忙，应说",
                    "[\"A. Can you help me?\",\"B. Help!\",\"C. Yes, please.\"]", "A",
                    "Can you help me? 是请求帮助的常用表达。", "交际用语"),
            new Seed(3, "speaking", "做错了事，应该说什么",
                    "[\"A. I'm sorry.\",\"B. Thank you.\",\"C. Good idea.\"]", "A",
                    "表达歉意用 I'm sorry。", "交际用语"),
            new Seed(3, "reading", "“book” 的同类词是",
                    "[\"A. pen\",\"B. run\",\"C. red\"]", "A", "pen 与 book 同属文具类名词。", "词义归类"),
            new Seed(3, "reading", "Lucy has a red pen and a blue bag. What color is the bag?",
                    "[\"A. Blue.\",\"B. Red.\",\"C. Green.\"]", "A", "原文提到 a blue bag。", "细节理解"),
            new Seed(3, "reading", "It is raining, so the ground is wet. “wet” 的意思是",
                    "[\"A. 湿的\",\"B. 干的\",\"C. 热的\"]", "A",
                    "由下雨可推测地面是湿的。", "词义猜测"),
            new Seed(3, "writing", "下列句子标点书写正确的是",
                    "[\"A. i like apples.\",\"B. I like apples.\",\"C. I like apples?\"]", "B",
                    "句首大写、陈述句以句号结尾。", "书写规范"),
            new Seed(3, "writing", "“this is my book” 的正确书写形式是",
                    "[\"A. This is my book.\",\"B. this is my book.\",\"C. This is my book?\"]", "A",
                    "句首首字母大写，陈述句以句号结尾。", "书写规范"),
            new Seed(3, "writing", "表达“我八岁了”，正确的句子是",
                    "[\"A. I am eight.\",\"B. I eight.\",\"C. I am eight year.\"]", "A",
                    "年龄表达用「主语 + be + 数字」。", "句式表达"),
            new Seed(3, "word", "选出拼写正确的单词",
                    "[\"A. beautiful\",\"B. betiful\",\"C. beutiful\"]", "A",
                    "beautiful 正确拼写为 b-e-a-u-t-i-f-u-l。", "单词拼写"),
            new Seed(3, "word", "选出拼写正确的单词",
                    "[\"A. friend\",\"B. freind\",\"C. frend\"]", "A",
                    "friend 中 ie 顺序为 i 在前 e 在后。", "单词拼写"),
            new Seed(3, "word", "“family” 的中文意思是",
                    "[\"A. 家庭\",\"B. 朋友\",\"C. 学校\"]", "A", "family 意为家庭。", "名词词义"),
            new Seed(3, "grammar", "“She ___ to school every day.” 应填",
                    "[\"A. go\",\"B. goes\",\"C. going\"]", "B", "第三人称单数的一般现在时动词加 -s。", "一般现在时"),
            new Seed(3, "grammar", "“They ___ football every Sunday.” 应填",
                    "[\"A. play\",\"B. plays\",\"C. playing\"]", "A", "复数主语的一般现在时动词用原形。", "一般现在时"),
            new Seed(3, "grammar", "“___ he like apples?” 应填",
                    "[\"A. Does\",\"B. Do\",\"C. Is\"]", "A", "第三人称单数用 Does 构成疑问句。", "一般疑问句"),

            // ==================== L4（小学·五六年级） ====================
            new Seed(4, "listening", "听对话：Where does Amy go on Sunday?",
                    "[\"A. She goes to the park.\",\"B. It is Sunday.\",\"C. Yes, she does.\"]", "A",
                    "问句询问地点，应答需给出地点信息。", "对话理解"),
            new Seed(4, "listening", "听对话：What time is it?",
                    "[\"A. It's seven.\",\"B. It's a clock.\",\"C. Yes, it is.\"]", "A",
                    "询问时间的应答需给出具体时刻。", "对话理解"),
            new Seed(4, "listening", "听短文：Tom goes to school by ___.",
                    "[\"A. bike\",\"B. book\",\"C. bag\"]", "A",
                    "by 后接交通工具，bike 符合语境。", "短文理解"),
            new Seed(4, "speaking", "店员说 “Can I help you?”，最合适的回答是",
                    "[\"A. Yes, I'd like a pen.\",\"B. You're welcome.\",\"C. See you.\"]", "A",
                    "应说明自己想买的物品。", "情景应答"),
            new Seed(4, "speaking", "向陌生人问路，应说",
                    "[\"A. Excuse me, where is the station?\",\"B. Where are you?\",\"C. The station is here.\"]", "A",
                    "问路先说 Excuse me 引起注意。", "情景应答"),
            new Seed(4, "speaking", "接电话时自我介绍，应说",
                    "[\"A. This is Tom speaking.\",\"B. I am Tom speaking.\",\"C. Here is Tom.\"]", "A",
                    "电话用语固定用 This is ... speaking。", "情景应答"),
            new Seed(4, "reading", "Mary gets up at six, has breakfast, then goes to school by bus. 主旨是",
                    "[\"A. Mary's morning\",\"B. Mary's bus\",\"C. Mary's school\"]", "A",
                    "全段按时间叙述 Mary 早晨的活动。", "主旨大意"),
            new Seed(4, "reading", "The library closes at five. We must go before five. When does it close?",
                    "[\"A. At five.\",\"B. At four.\",\"C. At six.\"]", "A", "原文提到 closes at five。", "细节理解"),
            new Seed(4, "reading", "Tom is ill today, so he is ___.（推断）",
                    "[\"A. at home\",\"B. at school\",\"C. in the park\"]", "A",
                    "生病通常在家休息。", "推理判断"),
            new Seed(4, "writing", "下列哪一项是完整句",
                    "[\"A. Because I was late.\",\"B. I was late for school.\",\"C. Although it rained.\"]", "B",
                    "完整句需有主谓结构且意思完整。", "句子结构"),
            new Seed(4, "writing", "邀请朋友一起去看电影，应该说",
                    "[\"A. Let's go to the cinema.\",\"B. Go cinema.\",\"C. We cinema.\"]", "A",
                    "Let's + 动词原形 用于提出邀请。", "句式表达"),
            new Seed(4, "writing", "下列日期书写规范的是",
                    "[\"A. June 1st\",\"B. june 1\",\"C. 1 June st\"]", "A",
                    "月份首字母大写，日用序数词。", "书写规范"),
            new Seed(4, "word", "选出与 big 意思相反的词",
                    "[\"A. small\",\"B. tall\",\"C. long\"]", "A", "big 的反义词是 small。", "反义词"),
            new Seed(4, "word", "选出与 begin 意思相反的词",
                    "[\"A. end\",\"B. start\",\"C. open\"]", "A", "begin 意为开始，反义为 end。", "反义词"),
            new Seed(4, "word", "“child” 的复数形式是",
                    "[\"A. children\",\"B. childs\",\"C. child\"]", "A",
                    "child 是不规则名词，复数为 children。", "名词复数"),
            new Seed(4, "grammar", "“They ___ football yesterday afternoon.” 应填",
                    "[\"A. play\",\"B. plays\",\"C. played\"]", "C", "yesterday afternoon 表示过去，用一般过去时。", "一般过去时"),
            new Seed(4, "grammar", "“I ___ to Beijing last year.” 应填",
                    "[\"A. went\",\"B. go\",\"C. gone\"]", "A", "last year 表示过去，go 的过去式为 went。", "一般过去时"),
            new Seed(4, "grammar", "“There ___ some milk in the glass.” 应填",
                    "[\"A. is\",\"B. are\",\"C. be\"]", "A", "milk 是不可数名词，用 is。", "There be 句型"),

            // ==================== L5（初中·初一初二） ====================
            new Seed(5, "listening", "听短文：The boy is looking for his ___.",
                    "[\"A. bag\",\"B. desk\",\"C. teacher\"]", "A",
                    "looking for 表示寻找，常搭配丢失的物品。", "短文理解"),
            new Seed(5, "listening", "听短文：Why is Tom late?",
                    "[\"A. He got up late.\",\"B. He is fine.\",\"C. Yes, he did.\"]", "A",
                    "Why 提问原因，应答需说明原因。", "短文理解"),
            new Seed(5, "listening", "听对话：How will they go there?",
                    "[\"A. By bus.\",\"B. It's far.\",\"C. Yes, they will.\"]", "A",
                    "How 提问方式，应答需给出交通方式。", "对话理解"),
            new Seed(5, "speaking", "想邀请同学一起踢球，应说",
                    "[\"A. Let's play football together.\",\"B. I play football.\",\"C. Football is good.\"]", "A",
                    "Let's ... 是提出邀请的常用句式。", "邀请表达"),
            new Seed(5, "speaking", "向朋友提出建议，应说",
                    "[\"A. Why not go with us?\",\"B. You go.\",\"C. Going now.\"]", "A",
                    "Why not + 动词原形 用于提建议。", "建议表达"),
            new Seed(5, "speaking", "表示同意对方的观点，应说",
                    "[\"A. I agree with you.\",\"B. I am agree.\",\"C. I agree you.\"]", "A",
                    "agree with sb. 是固定搭配。", "观点表达"),
            new Seed(5, "reading", "It is raining heavily, so the match is ___.（推断）",
                    "[\"A. put off\",\"B. started\",\"C. finished\"]", "A",
                    "由因果连词 so 与天气情况可推断比赛被推迟。", "推理判断"),
            new Seed(5, "reading", "He is very brave; he is not afraid of anything. “brave” 的意思是",
                    "[\"A. 勇敢的\",\"B. 害怕的\",\"C. 疲倦的\"]", "A",
                    "由 not afraid 可推知为勇敢。", "词义猜测"),
            new Seed(5, "reading", "The train leaves at 8:30, so we must arrive before 8:15. 到达时间应是",
                    "[\"A. Before 8:15.\",\"B. At 8:30.\",\"C. After 8:30.\"]", "A",
                    "原文明确给出 before 8:15。", "细节理解"),
            new Seed(5, "writing", "连词成句：school / I / to / go / by / bus",
                    "[\"A. I go to school by bus.\",\"B. I by bus go to school.\",\"C. Go to school I by bus.\"]", "A",
                    "英语基本语序为主语 + 谓语 + 宾语 + 方式状语。", "语序"),
            new Seed(5, "writing", "把 She is tall. 改为否定句",
                    "[\"A. She is not tall.\",\"B. She not is tall.\",\"C. Not she is tall.\"]", "A",
                    "含 be 动词的句子变否定，在 be 后加 not。", "句式转换"),
            new Seed(5, "writing", "下列书信结尾书写正确的是",
                    "[\"A. Yours,\",\"B. Your,\",\"C. You,\"]", "A",
                    "英文书信结尾常用 Yours, / Yours sincerely,。", "应用文写作"),
            new Seed(5, "word", "“careful” 的副词形式是",
                    "[\"A. carefully\",\"B. carefullly\",\"C. carefuly\"]", "A",
                    "形容词变副词一般在词尾加 -ly。", "构词法"),
            new Seed(5, "word", "“happy” 的名词形式是",
                    "[\"A. happiness\",\"B. happyness\",\"C. happyment\"]", "A",
                    "以辅音字母 + y 结尾，变 y 为 i 再加 -ness。", "构词法"),
            new Seed(5, "word", "“friend” 的形容词形式是",
                    "[\"A. friendly\",\"B. friendful\",\"C. friendy\"]", "A",
                    "名词加 -ly 可构成形容词，friendly 意为友好的。", "构词法"),
            new Seed(5, "grammar", "“Look! The children ___ in the park.” 应填",
                    "[\"A. play\",\"B. are playing\",\"C. played\"]", "B",
                    "Look! 提示动作正在发生，用现在进行时。", "现在进行时"),
            new Seed(5, "grammar", "“I ___ when he came in.” 应填",
                    "[\"A. was reading\",\"B. read\",\"C. am reading\"]", "A",
                    "过去某一时刻正在进行的动作，用过去进行时。", "过去进行时"),
            new Seed(5, "grammar", "“We ___ here since 2020.” 应填",
                    "[\"A. have lived\",\"B. lived\",\"C. live\"]", "A",
                    "since 引导的时间状语常与现在完成时连用。", "现在完成时"),

            // ==================== L6（初中·初三） ====================
            new Seed(6, "listening", "听短文，选择最佳标题",
                    "[\"A. A Day at School\",\"B. My Cat\",\"C. The Weather\"]", "A",
                    "短文围绕一天的校园生活展开。", "主旨概括"),
            new Seed(6, "listening", "听短文：What is the speaker's advice?",
                    "[\"A. Read more books.\",\"B. Books are cheap.\",\"C. Yes, it is.\"]", "A",
                    "advice 提问建议，应答需给出建议内容。", "短文理解"),
            new Seed(6, "listening", "听对话：Where are they talking?",
                    "[\"A. In a library.\",\"B. It's quiet.\",\"C. Yes, they are.\"]", "A",
                    "Where 提问地点，应答需给出地点。", "对话理解"),
            new Seed(6, "speaking", "表达观点：I think reading is ___.",
                    "[\"A. useful\",\"B. a book\",\"C. read\"]", "A",
                    "be 动词后接形容词作表语，说明看法。", "观点表达"),
            new Seed(6, "speaking", "委婉表达不同意见，应说",
                    "[\"A. I'm afraid I don't agree.\",\"B. You are wrong.\",\"C. No, I don't.\"]", "A",
                    "I'm afraid ... 可使语气更礼貌。", "观点表达"),
            new Seed(6, "speaking", "演讲的开场白，最恰当的是",
                    "[\"A. Today I'd like to talk about...\",\"B. I want say...\",\"C. My topic talk...\"]", "A",
                    "I'd like to ... 是正式演讲的常用开场。", "演讲表达"),
            new Seed(6, "reading", "作者认为阅读很有用，其语气是",
                    "[\"A. positive\",\"B. negative\",\"C. unsure\"]", "A",
                    "“有用”属于正面评价，语气积极。", "作者态度"),
            new Seed(6, "reading", "He never gives up when facing difficulties. 这句话说明他",
                    "[\"A. 坚持不懈\",\"B. 轻易放弃\",\"C. 粗心大意\"]", "A",
                    "never gives up 意为从不放弃。", "推理判断"),
            new Seed(6, "reading", "Recycling saves energy and reduces waste... 最佳标题是",
                    "[\"A. The Benefits of Recycling\",\"B. How to Make Paper\",\"C. Where to Buy Bottles\"]", "A",
                    "段落围绕回收利用的好处展开。", "主旨大意"),
            new Seed(6, "writing", "把 He likes apples. 改为一般疑问句",
                    "[\"A. Does he like apples?\",\"B. Likes he apples?\",\"C. Do he like apples?\"]", "A",
                    "第三人称单数的一般现在时用 Does 开头，动词还原。", "句式转换"),
            new Seed(6, "writing", "把 Tom said, \"I am busy.\" 改为间接引语",
                    "[\"A. Tom said he was busy.\",\"B. Tom said I am busy.\",\"C. Tom said he is busy now.\"]", "A",
                    "间接引语需变换人称并时态后退一格。", "引语转换"),
            new Seed(6, "writing", "下列衔接词使用正确的是",
                    "[\"A. However\",\"B. Because so\",\"C. Although but\"]", "A",
                    "英语中 because 与 so、although 与 but 不能同时使用。", "语篇衔接"),
            new Seed(6, "word", "选出与 happy 意思最接近的词",
                    "[\"A. glad\",\"B. sad\",\"C. angry\"]", "A", "glad 与 happy 都表示高兴。", "近义词"),
            new Seed(6, "word", "“important” 的名词形式是",
                    "[\"A. importance\",\"B. importancy\",\"C. importantment\"]", "A",
                    "以 -ant 结尾的形容词，名词多为 -ance 形式。", "构词法"),
            new Seed(6, "word", "“succeed” 的名词形式是",
                    "[\"A. success\",\"B. succession\",\"C. succeedment\"]", "A",
                    "succeed 的名词为 success。", "构词法"),
            new Seed(6, "grammar", "“If it ___ tomorrow, we will stay at home.” 应填",
                    "[\"A. rains\",\"B. will rain\",\"C. rained\"]", "A",
                    "条件状语从句遵循主将从现，从句用一般现在时。", "条件状语从句"),
            new Seed(6, "grammar", "“The book ___ by Lu Xun.” 应填",
                    "[\"A. was written\",\"B. wrote\",\"C. is writing\"]", "A",
                    "书是被写的，用一般过去时的被动语态。", "被动语态"),
            new Seed(6, "grammar", "“The man ___ is talking is my teacher.” 应填",
                    "[\"A. who\",\"B. which\",\"C. whose\"]", "A",
                    "先行词是人且在从句中作主语，用 who。", "定语从句")
    );

    @Override
    public void run(String... args) {
        backfill();
        seedToTarget();
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

    /**
     * 按「等级 × 学科」补种到 {@link #TARGET_PER_CELL} 题。
     * 已存在的题干会被跳过，因此重复启动不会重复灌数据。
     */
    private void seedToTarget() {
        Set<String> existingStems = questionRepo.findAll().stream()
                .map(Question::getStem)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, List<Seed>> grouped = SEEDS.stream()
                .collect(Collectors.groupingBy(s -> s.level() + "|" + s.subject(),
                        LinkedHashMap::new, Collectors.toList()));

        List<Question> toSave = new ArrayList<>();
        for (Map.Entry<String, List<Seed>> entry : grouped.entrySet()) {
            Seed first = entry.getValue().get(0);
            long existing = questionRepo.countByLevelAndSubject(first.level(), first.subject());
            int need = TARGET_PER_CELL - (int) existing;
            if (need <= 0) {
                continue;
            }
            int added = 0;
            for (Seed s : entry.getValue()) {
                if (added >= need) {
                    break;
                }
                if (existingStems.contains(s.stem())) {
                    continue;
                }
                toSave.add(toQuestion(s));
                existingStems.add(s.stem());
                added++;
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
