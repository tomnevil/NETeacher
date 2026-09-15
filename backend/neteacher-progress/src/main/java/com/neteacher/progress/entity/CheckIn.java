package com.neteacher.progress.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "check_date"}))
@Data
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    private LocalDateTime createdAt = LocalDateTime.now();
}
