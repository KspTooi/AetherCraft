package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.SavePreferencesDto;
import com.ksptool.ql.biz.model.vo.GetPreferencesVo;
import com.ksptool.ql.biz.service.UserConfigService;
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
    private UserConfigService userConfigService;

    @PostMapping("savePreferences")
    public Result<String> savePreferences(@RequestBody @Valid SavePreferencesDto dto) {

        if(StringUtils.isNotBlank(dto.getClientPath())) {
            userConfigService.setValue(UserConfigEnum.USER_PREF_CLIENT_PATH.key(), dto.getClientPath());
        }

        return Result.success("success");
    }


    @PostMapping("getPreferences")
    public Result<GetPreferencesVo> getPreferences(){

        var ret = new GetPreferencesVo();

        String clientPath = userConfigService.getValue(UserConfigEnum.USER_PREF_CLIENT_PATH.key());

        if(StringUtils.isNotBlank(clientPath)) {
            ret.setClientPath(clientPath);
        }

        return Result.success(ret);
    }


}
