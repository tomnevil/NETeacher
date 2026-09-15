package com.neteacher.user.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校（组织模型 DE-ORG-001）。
 */
@Getter
@Setter
@Entity
@Table(name = "school")
public class School extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    /** 学段：PRIMARY 小学 / JUNIOR 初中（用于约束可选年级范围） */
    @Column(length = 16)
    private String stage;

    private String city;

    @Column(length = 64)
    private String contact;
}
