package com.neteacher.user.dto;

import lombok.Data;

/**
 * 单题作答：选项下标（0-based）。
 */
@Data
public class PlacementAnswerDTO {
    private String questionId;
    private Integer selected;
}
