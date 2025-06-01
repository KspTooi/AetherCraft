package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.GetModelConfigDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelConfigDto;
import com.ksptool.ql.biz.model.dto.TestModelConnectionDto;
import com.ksptool.ql.biz.model.vo.GetAdminModelConfigVo;
import com.ksptool.ql.biz.model.vo.GetAvailableModelVo;
import com.ksptool.ql.biz.service.admin.AdminModelConfigService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/model")
public class AdminModelConfigController {

    @Autowired
    private AdminModelConfigService service;

    @PostMapping("getAvailableModels")
    public Result<List<GetAvailableModelVo>> getAvailableModels() {
        List<GetAvailableModelVo> modelList = new ArrayList<>();

        for (AIModelEnum model : AIModelEnum.values()) {
            GetAvailableModelVo vo = new GetAvailableModelVo();
            vo.setModelCode(model.getCode());
            vo.setModelName(model.getName());
            modelList.add(vo);
        }

        return Result.success(modelList);
    }

    @PostMapping("getModelConfig")
    @RequirePermissionRest("admin:model:view")
    public Result<GetAdminModelConfigVo> getModelConfig(@RequestBody @Valid GetModelConfigDto dto) {
        try {
            return Result.success(service.getModelConfig(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("saveModelConfig")
    @RequirePermissionRest("admin:model:edit")
    public Result<String> saveModelConfig(@RequestBody @Valid SaveAdminModelConfigDto dto) {
        try {
            service.saveModelConfig(dto);
            return Result.success("success");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("testModelConnection")
    @RequirePermissionRest("admin:model:test")
    public Result<String> testModelConnection(@RequestBody @Valid TestModelConnectionDto dto) {
        try {
            return Result.success(service.testModelConnection(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

}
