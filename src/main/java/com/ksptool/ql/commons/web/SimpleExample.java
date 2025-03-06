package com.ksptool.ql.commons.web;

import com.ksptool.entities.Entities;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;


/**
 * 简单Example查询构建器
 * @param <T> 实体类型
 */
public class SimpleExample<T> {
    private final T probe;
    private final List<String> likeFields;
    private Sort sort;

    private SimpleExample(T probe) {
        this.probe = probe;
        this.likeFields = new ArrayList<>();
        this.sort = Sort.unsorted();
    }

    public static <T> SimpleExample<T> of(T probe) {
        if (probe == null) {
            throw new IllegalArgumentException("查询实体不能为空");
        }
        return new SimpleExample<>(probe);
    }

    public SimpleExample<T> assign(Object source) {
        if (source != null) {
            Entities.assign(source, probe);
        }
        return this;
    }

    public SimpleExample<T> like(String... fields) {
        if (fields == null) {
            return this;
        }

        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                likeFields.add(field.trim());
            }
        }
        return this;
    }

    public SimpleExample<T> orderBy(String property) {
        this.sort = Sort.by(property);
        return this;
    }

    public SimpleExample<T> orderByDesc(String property) {
        this.sort = Sort.by(Sort.Direction.DESC, property);
        return this;
    }

    public Example<T> get() {
        if (likeFields.isEmpty()) {
            return Example.of(probe);
        }

        ExampleMatcher matcher = ExampleMatcher.matching();
        for (String field : likeFields) {
            matcher = matcher.withMatcher(field, ExampleMatcher.GenericPropertyMatchers.contains());
        }
        return Example.of(probe, matcher);
    }

    public Sort getSort() {
        return this.sort;
    }
} 