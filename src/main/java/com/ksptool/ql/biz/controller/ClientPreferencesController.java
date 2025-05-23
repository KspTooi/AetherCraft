package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.SavePreferencesDto;
import com.ksptool.ql.biz.model.vo.GetPreferencesVo;
import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientPreferencesController {

    @Autowired
    private PlayerConfigService playerConfigService;

    @PostMapping("savePreferences")
    public Result<String> savePreferences(@RequestBody @Valid SavePreferencesDto dto) {

        if(StringUtils.isNotBlank(dto.getClientPath())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_CLIENT_PATH.key(), dto.getClientPath());
        }

        if(StringUtils.isNotBlank(dto.getCustomizePathSide())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_SIDE.key(), dto.getCustomizePathSide());
        }

        if(StringUtils.isNotBlank(dto.getCustomizePathTabWallpaper())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_TAB_WALLPAPER.key(), dto.getCustomizePathTabWallpaper());
        }

        if(StringUtils.isNotBlank(dto.getCustomizePathTabTheme())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_TAB_THEME.key(), dto.getCustomizePathTabTheme());
        }

        if(StringUtils.isNotBlank(dto.getModelRoleEditCurrentId())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_MRE_CURRENT_ID.key(), dto.getModelRoleEditCurrentId());
        }

        if(StringUtils.isNotBlank(dto.getModelRoleEditPathTab())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_MRE_PATH_TAB.key(), dto.getModelRoleEditPathTab());
        }
        
        if(StringUtils.isNotBlank(dto.getClientRpPath())) {
            playerConfigService.put(UserConfigEnum.USER_PREF_CLIENT_RP_PATH.key(), dto.getClientRpPath());
        }

        return Result.success("success");
    }

    @PostMapping("getPreferences")
    public Result<GetPreferencesVo> getPreferences(){
        var ret = new GetPreferencesVo();

        String clientPath = playerConfigService.getString(UserConfigEnum.USER_PREF_CLIENT_PATH.key(),null);
        String customizePathSide = playerConfigService.getString(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_SIDE.key(),null);
        String customizePathTabWallpaper = playerConfigService.getString(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_TAB_WALLPAPER.key(),null);
        String customizePathTabTheme = playerConfigService.getString(UserConfigEnum.USER_PREF_CUSTOMIZE_PATH_TAB_THEME.key(),null);
        String modelRoleEditCurrentId = playerConfigService.getString(UserConfigEnum.USER_PREF_MRE_CURRENT_ID.key(),null);
        String modelRoleEditPathTab = playerConfigService.getString(UserConfigEnum.USER_PREF_MRE_PATH_TAB.key(),null);
        String clientRpPath = playerConfigService.getString(UserConfigEnum.USER_PREF_CLIENT_RP_PATH.key(),null);

        if(StringUtils.isNotBlank(clientPath)) {
            ret.setClientPath(clientPath);
        }

        if(StringUtils.isNotBlank(customizePathSide)) {
            ret.setCustomizePathSide(customizePathSide);
        }

        if(StringUtils.isNotBlank(customizePathTabWallpaper)) {
            ret.setCustomizePathTabWallpaper(customizePathTabWallpaper);
        }

        if(StringUtils.isNotBlank(customizePathTabTheme)) {
            ret.setCustomizePathTabTheme(customizePathTabTheme);
        }
        
        if(StringUtils.isNotBlank(modelRoleEditCurrentId)) {
            ret.setModelRoleEditCurrentId(modelRoleEditCurrentId);
        }

        if(StringUtils.isNotBlank(modelRoleEditPathTab)) {
            ret.setModelRoleEditPathTab(modelRoleEditPathTab);
        }
        
        if(StringUtils.isNotBlank(clientRpPath)) {
            ret.setClientRpPath(clientRpPath);
        }

        return Result.success(ret);
    }
}
