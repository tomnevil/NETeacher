package com.neteacher.common.base;

/**
 * 软删除标记接口。需要逻辑删除的实体实现本接口，由仓库层统一过滤。
 */
public interface Tenants {

    /** 逻辑删除标记：0=有效 1=已删除 */
    Integer getDeleted();

    void setDeleted(Integer deleted);
}
