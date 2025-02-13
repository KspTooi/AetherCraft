package com.ksptool.ql.commons.web;

import lombok.Data;

import java.util.List;

/**
 * 通用分页视图响应对象
 * @param <T> 数据行的类型
 */
@Data
public class PageableView<T> {
    
    /**
     * 数据行列表
     */
    private List<T> rows;
    
    /**
     * 总记录数
     */
    private long count;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 每页大小
     */
    private int pageSize;

    public PageableView() {
    }

    public PageableView(List<T> rows, long count, int currentPage, int pageSize) {
        this.rows = rows;
        this.count = count;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

}
