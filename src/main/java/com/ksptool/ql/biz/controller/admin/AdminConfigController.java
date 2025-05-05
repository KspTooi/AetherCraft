package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetConfigListDto;
import com.ksptool.ql.biz.model.dto.SaveConfigDto;
import com.ksptool.ql.biz.model.vo.GetConfigDetailsVo;
import com.ksptool.ql.biz.model.vo.GetConfigListVo;
import com.ksptool.ql.biz.service.admin.AdminConfigService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/config")
public class AdminConfigController {

    @Autowired
    private AdminConfigService service;

    @PostMapping("getConfigList")
    @RequirePermissionRest("admin:config:view")
    public Result<RestPageableView<GetConfigListVo>> getConfigList(@RequestBody @Valid GetConfigListDto dto){
        return Result.success(service.getConfigList(dto));
    }

    @PostMapping("getConfigDetails")
    @RequirePermissionRest("admin:config:save")
    public Result<GetConfigDetailsVo> getConfigDetails(@RequestBody @Valid CommonIdDto dto){
        try {
            return Result.success(service.getConfigDetails(dto.getId()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("saveConfig")
    @RequirePermissionRest("admin:config:save")
    public Result<String> saveConfig(@RequestBody @Valid SaveConfigDto dto){
        try {
            service.saveConfig(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeConfig")
    @RequirePermissionRest("admin:config:remove")
    public Result<String> removeConfig(@RequestBody @Valid CommonIdDto dto){
        try {
            service.removeConfig(dto.getId());
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

}
