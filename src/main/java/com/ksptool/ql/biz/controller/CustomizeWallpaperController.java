package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.WallpaperDto;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.biz.model.vo.WallpaperVo;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.enums.WallpaperEnum;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/customize")
public class CustomizeWallpaperController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * 获取所有默认壁纸
     * 
     * @return 壁纸列表
     */
    @GetMapping("/wallpaper/defaults")
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
} 