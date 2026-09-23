package com.neteacher.assessment.repository;

import com.neteacher.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    List<Question> findByLevelAndSubject(Integer level, String subject);

    List<Question> findByLevel(Integer level);

    List<Question> findBySubject(String subject);

    /** 用于按「等级 × 学科」缺口补种：0 表示该组合尚无题目 */
    long countByLevelAndSubject(Integer level, String subject);
}
