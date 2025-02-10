package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileItemVo {
    private String name;        // 文件名
    private String path;        // 完整路径
    private int kind;          // 类型：0-驱动器，1-目录，2-文件
    private String size;       // 文件大小（格式化后的字符串）
    private String lastModified; // 最后修改时间（格式化后的字符串）

    // 用于面包屑导航的简化构造函数
    public FileItemVo(String name, String path, int kind) {
        this.name = name;
        this.path = path;
        this.kind = kind;
    }
} 