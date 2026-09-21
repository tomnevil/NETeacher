package com.neteacher.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** AI 出题结果：实际提供方差 + 落库的草稿列表。 */
@Data
public class GenerateResultDTO {

    /** 出题提供方，如 mock / deepseek，便于追溯与前端提示 */
    private String provider;

    /** 落库的草稿数量 */
    private int generated;

    private List<QuestionDTO> questions = new ArrayList<>();
}
