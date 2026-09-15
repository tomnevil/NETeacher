package com.neteacher.user.repository;

import com.neteacher.user.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findByStage(String stage);
}
