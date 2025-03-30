package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.SaveColorStyleDto;
import com.ksptool.ql.biz.model.dto.WallpaperDto;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.biz.service.GlobalConfigService;
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
    private final UserConfigService userConfigService;
    private final GlobalConfigService globalConfigService;
    
    // 用户壁纸路径的配置键
    private static final String USER_WALLPAPER_PATH_KEY = "customize.wallpaper.path";
    
    @GetMapping("/view")
    public ModelAndView customizeView() {
        return new ModelAndView("user-customize/user-customize.html");
    }


    @PostMapping("/saveColorTheme")
    public Result<?> saveColorTheme(@RequestBody SaveColorStyleDto dto) {

        return null;
    }


} 