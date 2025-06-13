package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.WallpaperDto;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.biz.model.vo.WallpaperVo;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.enums.WallpaperEnum;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@PrintLog
@RestController
@RequestMapping("/customize")
public class CustomizeWallpaperController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private PlayerConfigService playerConfigService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @PostMapping("/resetWallpaper")
    public Result<?> resetWallpaper() {
        try {
            // 通过设置为 null 来移除壁纸配置项
            playerConfigService.put(UserConfigEnum.WALLPAPER_PATH.key(), (String) null);
            return Result.success("已恢复默认壁纸");
        } catch (Exception e) {
            return Result.error("恢复默认壁纸失败：" + e.getMessage());
        }
    }

    @GetMapping("/getWallpaper")
    public ResponseEntity<?> getWallpaper(@RequestParam(value = "check", required = false) Boolean check) {
        try {
            // 获取全局配置的缓存时间和默认壁纸路径
            int cacheSeconds = globalConfigService.getInt("customize.wallpaper.cache.seconds", 60);

            String defaultWallpaperPath = globalConfigService.get("customize.wallpaper.default.path","/img/bg2.jpg");

            // 从用户配置中获取壁纸路径
            String wallpaperPath = playerConfigService.getString(UserConfigEnum.WALLPAPER_PATH.key(),null);
            if (!StringUtils.hasText(wallpaperPath)) {
                return ResponseEntity.status(302)
                        .header("Location", defaultWallpaperPath)
                        .header("Cache-Control", "public, max-age=" + cacheSeconds)
                        .build();
            }

            // 通过统一文件系统获取文件
            File wallpaperFile = userFileService.getUserFile(wallpaperPath);
            if (wallpaperFile == null || !wallpaperFile.exists()) {
                return ResponseEntity.status(302)
                        .header("Location", defaultWallpaperPath)
                        .header("Cache-Control", "public, max-age=" + cacheSeconds)
                        .build();
            }

            // 如果是检查模式，直接返回成功
            if (Boolean.TRUE.equals(check)) {
                return ResponseEntity.ok().build();
            }

            // 响应图片文件，添加下载相关的响应头
            Resource resource = new FileSystemResource(wallpaperFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Content-Disposition", "inline; filename=wallpaper.jpg")
                    .header("Cache-Control", "public, max-age=" + cacheSeconds)
                    .body(resource);

        } catch (Exception e) {
            int cacheSeconds = globalConfigService.getInt("customize.wallpaper.cache.seconds", 60);
            String defaultWallpaperPath = globalConfigService.get("customize.wallpaper.default.path","/img/bg1.jpg");

            return ResponseEntity.status(302)
                    .header("Location", defaultWallpaperPath)
                    .header("Cache-Control", "public, max-age=" + cacheSeconds)
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
            MultipartFile multipartFile = new CustomizeWallpaperController.Base64MultipartFile(imageBytes);
            UserFilePo userFile = userFileService.receive(multipartFile);

            // 3. 更新用户配置，保存壁纸路径
            playerConfigService.put(UserConfigEnum.WALLPAPER_PATH.key(), userFile.getFilepath());

            return Result.success("壁纸设置成功");
        } catch (Exception e) {
            return Result.error("壁纸设置失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有默认壁纸
     * 
     * @return 壁纸列表
     */
    @GetMapping("/getDefaultWallpaper")
    public Result<List<WallpaperVo>> getDefaultWallpaper() {
        List<WallpaperVo> wallpapers = new ArrayList<>();
        
        for (WallpaperEnum wallpaper : WallpaperEnum.values()) {
            WallpaperVo vo = new WallpaperVo();
            vo.setPath("/img/" + wallpaper.key());
            vo.setName(wallpaper.defaultValue());
            wallpapers.add(vo);
        }
        
        return Result.success(wallpapers);
    }


    // 用于处理Base64图片数据的内部类
    private record Base64MultipartFile(byte[] fileContent) implements MultipartFile {

        @NotNull
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

        @NotNull
        @Override
        public byte[] getBytes() {
            return fileContent;
        }

        @NotNull
        @Override
        public java.io.InputStream getInputStream() {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(@NotNull File dest) throws java.io.IOException {
            throw new UnsupportedOperationException("transferTo() is not supported");
        }
    }

} 