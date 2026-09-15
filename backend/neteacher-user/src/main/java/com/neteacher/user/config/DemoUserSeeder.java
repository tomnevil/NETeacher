package com.neteacher.user.config;

import com.neteacher.common.util.PasswordUtil;
import com.neteacher.user.entity.ClassGroup;
import com.neteacher.user.entity.School;
import com.neteacher.user.entity.TeacherClass;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.ClassGroupRepository;
import com.neteacher.user.repository.SchoolRepository;
import com.neteacher.user.repository.TeacherClassRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 演示账号与组织种子。
 * 学生：13800000000 / 123456（三年级，归属「阳光小学 三年级1班」）
 * 家长：13900000000 / 123456（绑定该学生）
 * 教师：13700000000 / 123456（三年级1班班主任 + 英语任课）
 */
@Component
@RequiredArgsConstructor
public class DemoUserSeeder implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final SchoolRepository schoolRepo;
    private final ClassGroupRepository classRepo;
    private final TeacherClassRepository teacherClassRepo;

    @Override
    public void run(String... args) {
        School school = schoolRepo.findAll().stream().findFirst().orElseGet(() -> {
            School s = new School();
            s.setName("阳光小学");
            s.setStage("PRIMARY");
            s.setCity("示例市");
            s.setContact("020-0000000");
            return schoolRepo.save(s);
        });

        UserAccount teacher = userRepo.findByPhone("13700000000").orElseGet(() -> {
            UserAccount t = new UserAccount();
            t.setPhone("13700000000");
            t.setPassword(PasswordUtil.hash("123456"));
            t.setNickname("王老师");
            t.setRole("TEACHER");
            t.setSchoolId(school.getId());
            t.setStatus(2);
            return userRepo.save(t);
        });

        ClassGroup clazz = classRepo.findBySchoolId(school.getId()).stream().findFirst().orElseGet(() -> {
            ClassGroup c = new ClassGroup();
            c.setName("三年级1班");
            c.setSchoolId(school.getId());
            c.setGrade(3);
            c.setHeadTeacherId(teacher.getId());
            return classRepo.save(c);
        });

        if (!teacherClassRepo.existsByTeacherIdAndClassId(teacher.getId(), clazz.getId())) {
            TeacherClass tc = new TeacherClass();
            tc.setTeacherId(teacher.getId());
            tc.setClassId(clazz.getId());
            tc.setSubject("ENGLISH");
            teacherClassRepo.save(tc);
        }
        if (teacher.getClassId() == null) {
            teacher.setClassId(clazz.getId());
            userRepo.save(teacher);
        }

        UserAccount parent = userRepo.findByPhone("13900000000").orElseGet(() -> {
            UserAccount p = new UserAccount();
            p.setPhone("13900000000");
            p.setPassword(PasswordUtil.hash("123456"));
            p.setNickname("演示家长");
            p.setRole("PARENT");
            p.setStatus(2);
            return userRepo.save(p);
        });

        UserAccount student = userRepo.findByPhone("13800000000").orElseGet(() -> {
            UserAccount u = new UserAccount();
            u.setPhone("13800000000");
            u.setPassword(PasswordUtil.hash("123456"));
            u.setNickname("演示学员");
            u.setRole("STUDENT");
            u.setGrade(3);
            u.setStatus(2);
            return userRepo.save(u);
        });
        student.setGrade(3);
        student.setSchoolId(school.getId());
        student.setClassId(clazz.getId());
        student.setParentId(parent.getId());
        userRepo.save(student);
    }
}
