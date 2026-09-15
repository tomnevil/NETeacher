package com.neteacher.progress.repository;

import com.neteacher.progress.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    List<CheckIn> findByUserIdAndCheckDateBetween(Long userId, LocalDate start, LocalDate end);

    long countByUserId(Long userId);
}
