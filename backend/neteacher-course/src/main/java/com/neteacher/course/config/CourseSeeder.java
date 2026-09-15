package com.neteacher.course.config;

import com.neteacher.course.entity.Course;
import com.neteacher.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示种子数据：首次启动且课程表为空时，按年级写入主修课程与专项练习。
 * - 主修课程（MAJOR/EXTENSION）：每个年级按 L1-L6 递进
 * - 专项练习（SPECIAL）：单词 WORD / 口语 SPEAKING / 听力 LISTENING / 阅读 READING / 语法 GRAMMAR
 */
@Component
@RequiredArgsConstructor
public class CourseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepo;

    private static final String[] GRADES = {"三年级", "四年级", "五年级"};

    @Override
    public void run(String... args) {
        if (courseRepo.count() > 0) {
            return;
        }
        List<Course> all = new ArrayList<>();
        for (int g = 3; g <= 5; g++) {
            all.addAll(majorCourses(g));
            all.addAll(specialCourses(g));
        }
        courseRepo.saveAll(all);
    }

    /** 主修 / 拓展课程：按年级给出 L1-L6 阶梯 */
    private List<Course> majorCourses(int grade) {
        String g = gradeName(grade);
        List<Course> list = new ArrayList<>();
        list.add(course(g + "·趣味启蒙", grade, 1, "MAJOR", null, 1800,
                "以歌曲、动画建立" + g + "英语语感。", 12, "启蒙,听力"));
        list.add(course(g + "·日常会话", grade, 2, "MAJOR", null, 2400,
                "覆盖家庭、学校、购物等高频场景句型。", 16, "口语,会话"));
        list.add(course(g + "·自然拼读", grade, 3, "EXTENSION", null, 2100,
                "系统训练 CVC 与常见字母组合发音规则。", 14, "拼读,发音"));
        list.add(course(g + "·看图说话", grade, 4, "EXTENSION", null, 2600,
                "从单句到语段，提升看图表达与逻辑。", 18, "口语,写作"));
        list.add(course(g + "·阅读进阶", grade, 5, "MAJOR", null, 3200,
                "分级读物精读，强化词汇与长句理解。", 20, "阅读,词汇"));
        list.add(course(g + "·综合运用", grade, 6, "MAJOR", null, 3000,
                "综合听说读写，衔接更高年级要求。", 22, "综合,应试"));
        return list;
    }

    /** 专项练习：单词 / 口语 / 听力 / 阅读 / 语法 */
    private List<Course> specialCourses(int grade) {
        String g = gradeName(grade);
        List<Course> list = new ArrayList<>();
        list.add(course(g + "·单词专项", grade, 2, "SPECIAL", "WORD", 1200,
                "高频词图卡记忆 + 听音选词训练。", 10, "单词,词汇"));
        list.add(course(g + "·口语专项", grade, 2, "SPECIAL", "SPEAKING", 1500,
                "跟读评分，逐词纠音，练地道语调。", 10, "口语,跟读"));
        list.add(course(g + "·听力专项", grade, 2, "SPECIAL", "LISTENING", 1500,
                "短对话与短文听辨，抓关键词。", 10, "听力,理解"));
        list.add(course(g + "·阅读专项", grade, 3, "SPECIAL", "READING", 1800,
                "短文精读 + 推断题训练。", 10, "阅读,理解"));
        list.add(course(g + "·语法专项", grade, 3, "SPECIAL", "GRAMMAR", 1800,
                "时态、词性、句型的专项突破。", 10, "语法,应试"));
        return list;
    }

    private String gradeName(int grade) {
        return grade >= 1 && grade <= GRADES.length + 2 ? GRADES[grade - 3] : ("G" + grade);
    }

    private Course course(String title, int grade, int level, String category, String topic,
                          int durationSec, String desc, int lessons, String tags) {
        Course c = new Course();
        c.setTitle(title);
        c.setGrade(grade);
        c.setLevel(level);
        c.setCategory(category);
        c.setTopic(topic);
        c.setDurationSec(durationSec);
        c.setDescription(desc);
        c.setLessonCount(lessons);
        c.setTags(tags);
        return c;
    }
}
