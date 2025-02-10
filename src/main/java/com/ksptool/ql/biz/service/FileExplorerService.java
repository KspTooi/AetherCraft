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
        if ("@".equals(path.trim())) {
            // 如果是@，表示根目录，返回null让listFiles处理盘符列表
            return null;
        }
        // 确保Windows盘符路径以反斜杠结尾
        String trimmedPath = path.trim();
        if (System.getProperty("os.name").toLowerCase().contains("win") 
            && trimmedPath.matches("^[A-Za-z]:$")) {
            return trimmedPath + "\\";
        }
        return trimmedPath;
    }

    /**
     * 列出指定目录下的文件和文件夹
     */
    public List<FileItemVo> listFiles(String dirPath) {
        // 如果是根目录，则列出所有盘符
        if (dirPath == null || dirPath.trim().isEmpty() || "@".equals(dirPath)) {
            return listRootDrives();
        }

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
     * 列出所有盘符
     */
    private List<FileItemVo> listRootDrives() {
        File[] roots = File.listRoots();
        if (roots == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(roots)
                .map(root -> {
                    String path = root.getAbsolutePath();
                    // 移除路径末尾的斜杠，避免双斜杠问题
                    if (path.endsWith("\\")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    String name = path.replace("\\", "");
                    // 创建特殊的盘符FileItemVo
                    FileItemVo item = new FileItemVo();
                    item.setName(name);
                    item.setPath(name);
                    item.setDirectory(true);
                    item.setDrive(true); // 标记为驱动器
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否是根路径
     */
    private boolean isRootPath(String path) {
        if (path == null || path.trim().isEmpty() || "@".equals(path)) {
            return true;
        }
        
        // Windows系统
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String trimmedPath = path.trim();
            // 检查是否是盘符根目录（例如 "C:\"）
            return trimmedPath.matches("^[A-Za-z]:\\\\?$");
        }
        
        // Linux/Unix系统
        return path.equals("/");
    }

    /**
     * 获取面包屑导航数据
     */
    public List<FileItemVo> getBreadcrumbs(String path) {
        if (path == null || path.trim().isEmpty() || "@".equals(path)) {
            return List.of(new FileItemVo("计算机", "@", true));
        }

        List<FileItemVo> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new FileItemVo("计算机", "@", true));
        
        Path currentPath = Paths.get(path).normalize();
        if (currentPath.getRoot() != null) {
            String rootPath = currentPath.getRoot().toString();
            if (rootPath.endsWith("\\")) {
                rootPath = rootPath.substring(0, rootPath.length() - 1);
            }
            breadcrumbs.add(new FileItemVo(rootPath, rootPath, true));
        }
        
        // 添加每一级目录
        for (Path component : currentPath) {
            if (currentPath.getRoot() != null && component.toString().equals(currentPath.getRoot().toString())) {
                continue; // 跳过根目录，因为已经添加过了
            }
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
        item.setName(file.getName().isEmpty() ? file.getPath() : file.getName());
        item.setPath(file.getAbsolutePath());
        item.setDirectory(file.isDirectory());
        item.setDrive(false); // 标记为非驱动器
        if (!item.isDrive()) { // 只有非驱动器才设置这些属性
            item.setSize(file.length());
            item.setLastModified(file.lastModified());
        }
        return item;
    }

    /**
     * 获取父目录路径
     */
    public String getParentPath(String currentPath) {
        if (isRootPath(currentPath)) {
            return "ROOT"; // 特殊标记，表示需要显示盘符列表
        }
        Path path = Paths.get(currentPath);
        Path parent = path.getParent();
        return parent != null ? parent.toString() : "ROOT";
    }
} 