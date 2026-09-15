package com.neteacher.user.service;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.user.dto.CreateClassRequest;
import com.neteacher.user.dto.OrgClass;
import com.neteacher.user.dto.OrgMember;
import com.neteacher.user.dto.OrgSchool;
import com.neteacher.user.entity.ClassGroup;
import com.neteacher.user.entity.School;
import com.neteacher.user.entity.TeacherClass;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.ClassGroupRepository;
import com.neteacher.user.repository.SchoolRepository;
import com.neteacher.user.repository.TeacherClassRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织模型服务（M1 扩展）：学校 / 班级 / 教师-班级关系。
 */
@Service
@RequiredArgsConstructor
public class OrgService {

    private final SchoolRepository schoolRepo;
    private final ClassGroupRepository classRepo;
    private final TeacherClassRepository teacherClassRepo;
    private final UserAccountRepository userRepo;

    public List<OrgSchool> listSchools() {
        List<OrgSchool> out = new ArrayList<>();
        for (School s : schoolRepo.findAll()) {
            OrgSchool dto = new OrgSchool();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setStage(s.getStage());
            dto.setCity(s.getCity());
            List<ClassGroup> classes = classRepo.findBySchoolId(s.getId());
            dto.setClassCount(classes.size());
            dto.setTeacherCount(userRepo.findBySchoolId(s.getId()).stream()
                    .filter(u -> "TEACHER".equals(u.getRole())).toList().size());
            dto.setStudentCount(userRepo.findBySchoolId(s.getId()).stream()
                    .filter(u -> "STUDENT".equals(u.getRole())).toList().size());
            out.add(dto);
        }
        return out;
    }

    public List<OrgClass> listClasses(Long schoolId, Integer grade) {
        List<ClassGroup> classes = schoolId != null
                ? classRepo.findBySchoolId(schoolId)
                : grade != null ? classRepo.findByGrade(grade) : classRepo.findAll();
        List<OrgClass> out = new ArrayList<>();
        for (ClassGroup c : classes) {
            out.add(toDto(c));
        }
        return out;
    }

    public List<OrgClass> classesOfTeacher(Long teacherId) {
        List<Long> ids = teacherClassRepo.findByTeacherId(teacherId).stream()
                .map(TeacherClass::getClassId).toList();
        List<OrgClass> out = new ArrayList<>();
        for (Long id : ids) {
            classRepo.findById(id).ifPresent(c -> out.add(toDto(c)));
        }
        return out;
    }

    public OrgClass createClass(CreateClassRequest req) {
        School school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "学校不存在"));
        ClassGroup c = new ClassGroup();
        c.setName(req.getName());
        c.setSchoolId(school.getId());
        c.setGrade(req.getGrade());
        c.setHeadTeacherId(req.getHeadTeacherId());
        classRepo.save(c);
        if (req.getHeadTeacherId() != null) {
            bindTeacher(req.getHeadTeacherId(), c.getId());
        }
        return toDto(c);
    }

    public void bindTeacher(Long teacherId, Long classId) {
        ClassGroup c = classRepo.findById(classId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "班级不存在"));
        UserAccount teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "教师不存在"));
        if (!teacherClassRepo.existsByTeacherIdAndClassId(teacherId, classId)) {
            TeacherClass tc = new TeacherClass();
            tc.setTeacherId(teacherId);
            tc.setClassId(classId);
            teacherClassRepo.save(tc);
        }
        if (c.getHeadTeacherId() == null) {
            c.setHeadTeacherId(teacherId);
            classRepo.save(c);
        }
        if (teacher.getClassId() == null) {
            teacher.setClassId(classId);
        }
        if (teacher.getSchoolId() == null) {
            teacher.setSchoolId(c.getSchoolId());
        }
        userRepo.save(teacher);
    }

    /** 将学生绑定到班级（同时补全其 schoolId） */
    public OrgMember bindStudent(Long studentId, Long classId) {
        ClassGroup c = classRepo.findById(classId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "班级不存在"));
        UserAccount student = userRepo.findById(studentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "学生不存在"));
        if (!"STUDENT".equals(student.getRole())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仅学生可绑定到班级");
        }
        student.setClassId(classId);
        student.setSchoolId(c.getSchoolId());
        if (c.getGrade() != null) {
            student.setGrade(c.getGrade());
        }
        userRepo.save(student);
        return toMember(student);
    }

    /** 将学生移出班级 */
    public void unbindStudent(Long studentId) {
        UserAccount student = userRepo.findById(studentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "学生不存在"));
        student.setClassId(null);
        userRepo.save(student);
    }

    /** 班级学生名单 */
    public List<OrgMember> studentsOfClass(Long classId) {
        return userRepo.findByClassId(classId).stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .map(this::toMember)
                .toList();
    }

    /** 未分配班级的学生（供后台挑选绑定） */
    public List<OrgMember> unassignedStudents() {
        return userRepo.findByRole("STUDENT").stream()
                .filter(u -> u.getClassId() == null)
                .map(this::toMember)
                .toList();
    }

    /** 学校下的教师列表 */
    public List<OrgMember> teachersOfSchool(Long schoolId) {
        return userRepo.findBySchoolId(schoolId).stream()
                .filter(u -> "TEACHER".equals(u.getRole()))
                .map(this::toMember)
                .toList();
    }

    private OrgMember toMember(UserAccount u) {
        OrgMember m = new OrgMember();
        m.setUid(u.getId());
        m.setNickname(u.getNickname());
        m.setPhone(u.getPhone());
        m.setRole(u.getRole());
        m.setGrade(u.getGrade());
        m.setClassId(u.getClassId());
        if (u.getClassId() != null) {
            classRepo.findById(u.getClassId()).ifPresent(c -> m.setClassName(c.getName()));
        }
        return m;
    }

    private OrgClass toDto(ClassGroup c) {
        OrgClass dto = new OrgClass();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSchoolId(c.getSchoolId());
        dto.setGrade(c.getGrade());
        dto.setHeadTeacherId(c.getHeadTeacherId());
        if (c.getSchoolId() != null) {
            schoolRepo.findById(c.getSchoolId()).ifPresent(s -> dto.setSchoolName(s.getName()));
        }
        if (c.getHeadTeacherId() != null) {
            userRepo.findById(c.getHeadTeacherId())
                    .ifPresent(t -> dto.setHeadTeacherName(t.getNickname()));
        }
        dto.setStudentCount(userRepo.findByClassId(c.getId()).stream()
                .filter(u -> "STUDENT".equals(u.getRole())).toList().size());
        return dto;
    }
}
