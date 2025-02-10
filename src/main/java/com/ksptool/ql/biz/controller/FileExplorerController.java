package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.vo.FileItemVo;
import com.ksptool.ql.biz.service.FileExplorerService;
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
} 