package com.ksptool.ql.commons.web;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {
    
    /**
     * 当前页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    /**
     * 每页记录数
     */
    @Min(value = 1, message = "每页记录数必须大于0")
    private Integer pageSize = 10;

    /**
     * 获取JPA分页请求对象
     */
    public PageRequest pageRequest() {
        return PageRequest.of(page - 1, pageSize);
    }
} 