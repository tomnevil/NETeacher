package com.neteacher.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 批量导入结果 */
@Data
public class ImportResultDTO {
    /** 成功导入条数 */
    private int imported = 0;
    /** 跳过条数 */
    private int skipped = 0;
    /** 错误/跳过明细（含行号与原因） */
    private List<String> errors = new ArrayList<>();
}
