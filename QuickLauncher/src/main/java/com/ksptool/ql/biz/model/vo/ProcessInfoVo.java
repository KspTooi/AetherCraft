package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class ProcessInfoVo {
    private String name;        // 进程名称
    private Long pid;          // 进程ID
    private String memory;     // 内存使用量(格式化后的字符串,如"128 MB")
    private String cpu;        // CPU使用率(格式化后的字符串,如"2.5%")
    private String status;     // 进程状态
} 