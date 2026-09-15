package com.neteacher.ops.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 会员订单/权益。plan 区分 FREE / PRO。
 */
@Getter
@Setter
@Entity
@Table(name = "membership")
public class Membership extends BaseEntity {

    private Long userId;

    /** FREE / PRO */
    private String plan;

    private LocalDate expireAt;
}
