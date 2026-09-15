package com.neteacher.user.repository;

import com.neteacher.user.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {

    List<ClassGroup> findBySchoolId(Long schoolId);

    List<ClassGroup> findByGrade(Integer grade);

    List<ClassGroup> findByHeadTeacherId(Long headTeacherId);
}
