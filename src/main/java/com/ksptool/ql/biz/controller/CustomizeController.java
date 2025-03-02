package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.WallpaperDto;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.biz.service.ConfigService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.File;

@RestController
@RequestMapping("/customize")
@RequiredArgsConstructor
public class CustomizeController {
    
    private final UserFileService userFileService;
    private final ConfigService configService;
    
    @GetMapping("/view")
    public ModelAndView customizeView() {
        return new ModelAndView("customize");
    }

    @PostMapping("/resetWallpaper")
    public Result<?> resetWallpaper() {
        try {
            // 通过设置为 null 来移除壁纸配置项
            configService.setValue("customize.wallpaper.path", null);
            return Result.success("已恢复默认壁纸");
        } catch (Exception e) {
            return Result.error("恢复默认壁纸失败：" + e.getMessage());
        }
    }

    @GetMapping("/wallpaper")
    public ResponseEntity<?> getWallpaper(@RequestParam(value = "check", required = false) Boolean check) {
        try {
            // 1. 从配置中获取壁纸路径
            String wallpaperPath = configService.get("customize.wallpaper.path");
            if (!StringUtils.hasText(wallpaperPath)) {
                return ResponseEntity.status(302)
                    .header("Location", "/img/bg1.jpg")
                    .header("Cache-Control", "public, max-age=60") // 缓存1分钟
                    .build();
            }

            // 2. 通过统一文件系统获取文件
            File wallpaperFile = userFileService.getUserFile(wallpaperPath);
            if (wallpaperFile == null || !wallpaperFile.exists()) {
                return ResponseEntity.status(302)
                    .header("Location", "/img/bg1.jpg")
                    .header("Cache-Control", "public, max-age=60") // 缓存1分钟
                    .build();
            }

            // 如果是检查模式，直接返回成功
            if (Boolean.TRUE.equals(check)) {
                return ResponseEntity.ok().build();
            }

            // 3. 响应图片文件，添加下载相关的响应头
            Resource resource = new FileSystemResource(wallpaperFile);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Content-Disposition", "inline; filename=wallpaper.jpg") // 修改为inline
                .header("Cache-Control", "public, max-age=60") // 缓存1分钟
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.status(302)
                .header("Location", "/img/bg1.jpg")
                .header("Cache-Control", "public, max-age=60") // 缓存1分钟
                .build();
        }
    }

    @PostMapping("/setWallpaper")
    public Result<?> setWallpaper(@Valid @RequestBody WallpaperDto dto) {
        try {
            // 1. 解码Base64图片数据
            String base64Image = dto.getImageData().split(",")[1]; // 移除 "data:image/jpeg;base64," 前缀
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 2. 将图片保存到文件系统
            MultipartFile multipartFile = new Base64MultipartFile(imageBytes);
            UserFilePo userFile = userFileService.receive(multipartFile);

            // 3. 更新用户配置，保存壁纸路径
            configService.setValue("customize.wallpaper.path", userFile.getFilepath());

            return Result.success("壁纸设置成功");
        } catch (Exception e) {
            return Result.error("壁纸设置失败：" + e.getMessage());
        }
    }

    // 用于处理Base64图片数据的内部类
    private static class Base64MultipartFile implements MultipartFile {
        private final byte[] fileContent;
        
        public Base64MultipartFile(byte[] fileContent) {
            this.fileContent = fileContent;
        }

        @Override
        public String getName() {
            return "wallpaper";
        }

        @Override
        public String getOriginalFilename() {
            return "wallpaper.jpg";
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return fileContent == null || fileContent.length == 0;
        }

        @Override
        public long getSize() {
            return fileContent.length;
        }

        @Override
        public byte[] getBytes() {
            return fileContent;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            throw new UnsupportedOperationException("transferTo() is not supported");
        }
    }
} 