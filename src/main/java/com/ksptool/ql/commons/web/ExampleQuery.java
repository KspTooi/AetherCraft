package com.ksptool.ql.commons.web;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 通用Example查询基类
 * @param <T> 实体类型
 */
public abstract class ExampleQuery<T> {

    /**
     * 获取查询实体
     * @return 查询实体
     */
    protected abstract T getProbe();

    /**
     * 构建Example查询条件
     * @return Example查询条件
     */
    public Example<T> toExample() {
        return Example.of(getProbe());
    }

    /**
     * 构建带有模糊查询的Example查询条件
     * @param like 需要模糊查询的字段名
     * @return Example查询条件
     */
    public Example<T> toExample(String... like) {
        if (like == null || like.length == 0) {
            return toExample();
        }

        ExampleMatcher matcher = ExampleMatcher.matching();
        for (String field : like) {
            if (field == null || field.trim().isEmpty()) {
                continue;
            }
            matcher = matcher.withMatcher(field.trim(), ExampleMatcher.GenericPropertyMatchers.contains());
        }
        return Example.of(getProbe(), matcher);
    }
} 