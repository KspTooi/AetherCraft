package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.vo.FileItemVo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileExplorerService {

    /**
     * 规范化路径
     */
    public String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            // 如果路径为空,返回用户目录
            return System.getProperty("user.home");
        }
        return path.trim();
    }

    /**
     * 列出指定目录下的文件和文件夹
     */
    public List<FileItemVo> listFiles(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .map(this::convertToFileItem)
                .sorted((a, b) -> {
                    // 文件夹排在前面
                    if (a.isDirectory() != b.isDirectory()) {
                        return a.isDirectory() ? -1 : 1;
                    }
                    // 同类型按名称排序
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取面包屑导航数据
     */
    public List<FileItemVo> getBreadcrumbs(String path) {
        List<FileItemVo> breadcrumbs = new ArrayList<>();
        Path currentPath = Paths.get(path).normalize();
        
        // 添加根目录
        breadcrumbs.add(new FileItemVo("根目录", currentPath.getRoot().toString(), true));
        
        // 添加每一级目录
        for (Path component : currentPath) {
            String fullPath = currentPath.getRoot().toString() + component;
            breadcrumbs.add(new FileItemVo(component.toString(), fullPath, true));
        }
        
        return breadcrumbs;
    }

    /**
     * 将File对象转换为FileItemVo
     */
    private FileItemVo convertToFileItem(File file) {
        FileItemVo item = new FileItemVo();
        item.setName(file.getName());
        item.setPath(file.getAbsolutePath());
        item.setDirectory(file.isDirectory());
        item.setSize(file.length());
        item.setLastModified(file.lastModified());
        return item;
    }

    /**
     * 获取父目录路径
     */
    public String getParentPath(String currentPath) {
        Path path = Paths.get(currentPath);
        Path parent = path.getParent();
        return parent != null ? parent.toString() : "";
    }
} 