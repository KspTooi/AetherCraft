package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.SaveColorStyleDto;
import com.ksptool.ql.biz.model.dto.SaveThemeSettingsDto;
import com.ksptool.ql.biz.model.vo.GetThemeSettingsVo;
import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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


    @PostMapping("saveThemeSettings")
    public Result<?> saveThemeSettings(@RequestBody SaveThemeSettingsDto dto) {


        return null;
    }

    @PostMapping("getThemeSettings")
    public Result<GetThemeSettingsVo> getThemeSettings() {

        userConfigService.get("","");


        return null;
    }


} 