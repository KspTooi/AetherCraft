package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.vo.FileItemVo;
import com.ksptool.ql.biz.model.dto.RenameFileDto;
import com.ksptool.ql.biz.service.FileExplorerService;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/ssr")
public class FileExplorerController {

    @Autowired
    private FileExplorerService fileExplorerService;

    @GetMapping("/fileExplorer")
    public ModelAndView fileExplorer(@RequestParam(value = "path", required = false) String path, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("file-explorer");
        
        try {
            // URL解码
            String decodedPath = path;
            if (path != null) {
                try {
                    decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    decodedPath = path;
                }
            }
            
            // 处理@符号（根目录）
            if ("@".equals(decodedPath)) {
                List<FileItemVo> files = fileExplorerService.listFiles(null);
                mav.addObject("currentPath", "@");
                mav.addObject("parentPath", "");
                mav.addObject("files", files);
                mav.addObject("breadcrumbs", List.of(new FileItemVo("计算机", "@", 0)));
                return mav;
            }
            
            // 规范化路径
            String currentPath = fileExplorerService.normalizePath(decodedPath);
            
            // 验证路径是否存在
            File dir = new File(currentPath);
            if (!dir.exists() || !dir.isDirectory()) {
                String parentPath = fileExplorerService.getParentPath(currentPath);
                if ("@".equals(parentPath)) {
                    redirectAttributes.addFlashAttribute("error", "路径不存在或不是有效的目录：" + currentPath);
                    return new ModelAndView("redirect:/ssr/fileExplorer?path=@");
                }
                if (parentPath == null || parentPath.isEmpty()) {
                    parentPath = System.getProperty("user.home");
                }
                String encodedPath = UriUtils.encodePath(parentPath, StandardCharsets.UTF_8.name());
                redirectAttributes.addFlashAttribute("error", "路径不存在或不是有效的目录：" + currentPath);
                return new ModelAndView("redirect:/ssr/fileExplorer?path=" + encodedPath);
            }
            
            // 获取当前目录的文件列表
            List<FileItemVo> files = fileExplorerService.listFiles(currentPath);
            
            // 获取面包屑导航数据
            List<FileItemVo> breadcrumbs = fileExplorerService.getBreadcrumbs(currentPath);
            
            // 获取父目录路径
            String parentPath = fileExplorerService.getParentPath(currentPath);
            
            mav.addObject("currentPath", currentPath);
            mav.addObject("parentPath", parentPath);
            mav.addObject("files", files);
            mav.addObject("breadcrumbs", breadcrumbs);
            
        } catch (Exception e) {
            // 发生异常时重定向到用户主目录
            String userHome = System.getProperty("user.home");
            // 对重定向路径进行编码
            try {
                String encodedPath = UriUtils.encodePath(userHome, StandardCharsets.UTF_8.name());
                redirectAttributes.addFlashAttribute("error", "访问路径时发生错误：" + e.getMessage());
                return new ModelAndView("redirect:/ssr/fileExplorer?path=" + encodedPath);
            } catch (Exception ex) {
                // 如果编码失败，使用默认编码
                return new ModelAndView("redirect:/ssr/fileExplorer");
            }
        }
        
        return mav;
    }

    @GetMapping("/validatePath")
    @ResponseBody
    public boolean validatePath(@RequestParam("path") String path) {
        try {
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            File file = new File(decodedPath);
            return file.exists() && file.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/renameFile")
    @ResponseBody
    public Result<String> renameFile(@RequestBody RenameFileDto dto) {
        try {
            // 参数验证
            if (dto.getNewName() == null || dto.getNewName().trim().isEmpty()) {
                return Result.error("新文件名不能为空");
            }
            
            File oldFile = new File(dto.getOldPath());
            if (!oldFile.exists()) {
                return Result.error("原文件不存在：" + dto.getOldPath());
            }

            // 获取父目录路径
            String parentPath = oldFile.getParent();
            if (parentPath == null) {
                return Result.error("无法获取父目录路径");
            }

            // 构建新文件路径
            File newFile = new File(parentPath, dto.getNewName());
            
            // 检查新文件名是否已存在
            if (newFile.exists()) {
                return Result.error("该名称已存在，请使用其他名称：" + dto.getNewName());
            }

            // 检查父目录是否可写
            File parentDir = new File(parentPath);
            if (!parentDir.exists()) {
                return Result.error("父目录不存在：" + parentPath);
            }
            if (!parentDir.canWrite()) {
                return Result.error("没有写入权限，请检查目录权限：" + parentPath);
            }

            // 检查文件是否可写
            if (!oldFile.canWrite()) {
                return Result.error("文件被占用或没有写入权限：" + dto.getOldPath());
            }

            // 验证新文件名格式
            if (dto.getNewName().contains("/") || dto.getNewName().contains("\\")) {
                return Result.error("新文件名不能包含路径分隔符：" + dto.getNewName());
            }
            if (dto.getNewName().matches(".*[<>:\"|?*].*")) {
                return Result.error("新文件名包含非法字符 (<>:\"|?*)：" + dto.getNewName());
            }

            // 执行重命名
            if (!oldFile.renameTo(newFile)) {
                return Result.error("重命名操作失败，可能是文件被占用或系统限制");
            }
            
            return Result.success("重命名成功");
        } catch (SecurityException e) {
            return Result.error("安全限制，无法重命名：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("重命名失败：" + e.getMessage());
        }
    }
} 