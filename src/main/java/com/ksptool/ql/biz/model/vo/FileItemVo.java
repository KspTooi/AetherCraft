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
    private boolean directory;  // 是否是目录
    private boolean drive;      // 是否是驱动器
    private long size;         // 文件大小
    private long lastModified; // 最后修改时间

    // 用于面包屑导航的简化构造函数
    public FileItemVo(String name, String path, boolean directory) {
        this.name = name;
        this.path = path;
        this.directory = directory;
        this.drive = false;
    }
} 