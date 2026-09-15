package com.neteacher.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClassRequest {

    @NotBlank(message = "班级名不能为空")
    private String name;

    @NotNull(message = "学校不能为空")
    private Long schoolId;

    @NotNull(message = "年级不能为空")
    private Integer grade;

    /** 可选：指定班主任 / 负责教师 */
    private Long headTeacherId;
}
