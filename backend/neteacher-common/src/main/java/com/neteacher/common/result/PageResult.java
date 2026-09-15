package com.neteacher.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应封装。
 */
@Data
public class PageResult<T> implements Serializable {

    private long total;
    private int page;
    private int size;
    private List<T> records;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.records = records;
        r.total = total;
        r.page = page;
        r.size = size;
        return r;
    }
}
