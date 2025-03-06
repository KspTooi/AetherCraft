package com.ksptool.ql.commons.web;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

import static com.ksptool.entities.Entities.as;

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

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrev;

    public PageableView() {
    }

    public PageableView(List<T> rows, long count, int currentPage, int pageSize) {
        this.rows = rows;
        this.count = count;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        calculatePaging();
    }

    public PageableView(Page<?> page, List<T> rows) {
        this.rows = rows;
        this.count = page.getTotalElements();
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
        calculatePaging();
    }

    public PageableView(Page<?> page, Class<T> targetClass) {
        this.rows = as(page.getContent(), targetClass);
        this.count = page.getTotalElements();
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
        calculatePaging();
    }

    private void calculatePaging() {
        // 计算是否有上一页和下一页
        this.hasPrev = currentPage > 1;
        int totalPages = (int) Math.ceil(count * 1.0 / pageSize);
        this.hasNext = currentPage < totalPages;
    }
}
