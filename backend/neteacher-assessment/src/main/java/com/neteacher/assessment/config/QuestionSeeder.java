package com.neteacher.assessment.config;

import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 演示题库种子：覆盖 L1-L3 的听/说/读/写基础题。
 */
@Component
@RequiredArgsConstructor
public class QuestionSeeder implements CommandLineRunner {

    private final QuestionRepository questionRepo;

    @Override
    public void run(String... args) {
        if (questionRepo.count() > 0) {
            return;
        }
        questionRepo.saveAll(java.util.List.of(
                q(1, "listening", "听音选词：/kæt/", "[\"A. cat\",\"B. cut\",\"C. kite\"]", "A", "字母 a 在闭音节发 /æ/。"),
                q(1, "listening", "听音选词：/dɒɡ/", "[\"A. dog\",\"B. dug\",\"C. bag\"]", "A", "o 在重读闭音节发 /ɒ/。"),
                q(2, "reading", "选出划线部分发音不同的词", "[\"A. name\",\"B. map\",\"C. cake\"]", "B", "a 在开音节发 /eɪ/，闭音节发 /æ/。", 2),
                q(2, "reading", "“apple” 的复数形式是", "[\"A. apple\",\"B. apples\",\"C. applees\"]", "B", "一般名词加 -s 构成复数。"),
                q(3, "speaking", "当你想请别人重复时，应说", "[\"A. Pardon?\",\"B. Stop.\",\"C. No.\"]", "A", "Pardon? 是礼貌地请求重复。"),
                q(3, "speaking", "向老师问好，应说", "[\"A. Hello, teacher.\",\"B. Good morning, Mr. Li.\",\"C. Hi boy.\"]", "B", "用职称+姓氏更得体。"),
                q(1, "writing", "“I ___ a student.” 应填", "[\"A. am\",\"B. is\",\"C. are\"]", "A", "I 搭配 am。"),
                q(2, "writing", "“He ___ ten years old.” 应填", "[\"A. am\",\"B. is\",\"C. are\"]", "B", "第三人称单数用 is。"),
                q(3, "listening", "听音选词：/ʃiːp/", "[\"A. ship\",\"B. sheep\",\"C. cheap\"]", "B", "ee 发长音 /iː/。"),
                q(3, "reading", "“book” 的同类是", "[\"A. pen\",\"B. run\",\"C. red\"]", "A", "pen 与 book 同属文具类名词。"),
                // 单词专项
                q(1, "word", "选出“苹果”的英文", "[\"A. apple\",\"B. banana\",\"C. orange\"]", "A", "apple 是苹果。"),
                q(2, "word", "“cat” 的中文意思是", "[\"A. 猫\",\"B. 狗\",\"C. 鸟\"]", "A", "cat 意为猫。"),
                q(3, "word", "选出拼写正确的词", "[\"A. beautiful\",\"B. betiful\",\"C. beutiful\"]", "A", "beautiful 正确拼写为 b-e-a-u-t-i-f-u-l。"),
                // 语法专项
                q(1, "grammar", "“I ___ a student.” 应填", "[\"A. am\",\"B. is\",\"C. are\"]", "A", "第一人称 I 搭配 am。"),
                q(2, "grammar", "“He ___ ten years old.” 应填", "[\"A. am\",\"B. is\",\"C. are\"]", "B", "第三人称单数用 is。"),
                q(3, "grammar", "“There ___ an apple on the desk.” 应填", "[\"A. is\",\"B. are\",\"C. am\"]", "A", "单数名词用 is。")
        ));
    }

    private Question q(int level, String subject, String stem, String options, String answer, String analysis) {
        return q(level, subject, stem, options, answer, analysis, 1);
    }

    private Question q(int level, String subject, String stem, String options, String answer, String analysis, int sizeHint) {
        Question question = new Question();
        question.setLevel(level);
        question.setSubject(subject);
        question.setStem(stem);
        question.setOptions(options);
        question.setAnswer(answer);
        question.setAnalysis(analysis);
        question.setType("mcq");
        return question;
    }
}
