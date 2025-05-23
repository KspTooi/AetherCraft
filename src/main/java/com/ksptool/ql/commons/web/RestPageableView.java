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
public class RestPageableView<T> {

    /**
     * 数据行列表
     */
    private List<T> rows;

    /**
     * 总记录数
     */
    private Integer count;


    public RestPageableView() {
    }

    public RestPageableView(List<T> rows, Long count) {
        this.rows = rows;
        this.count = Math.toIntExact(count);
    }

    public RestPageableView(Page<?> page, List<T> rows) {
        this.rows = rows;
        this.count = Math.toIntExact(page.getTotalElements());
    }

    public RestPageableView(Page<T> page) {
        this.rows = page.getContent();
        this.count = Math.toIntExact(page.getTotalElements());
    }

    public RestPageableView(Page<?> page, Class<T> targetClass) {
        this.rows = as(page.getContent(), targetClass);
        this.count = Math.toIntExact(page.getTotalElements());
    }


}
