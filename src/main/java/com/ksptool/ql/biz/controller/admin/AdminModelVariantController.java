package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.AdminToggleModelVariantDto;
import com.ksptool.ql.biz.model.dto.ApplyModelVariantParamTemplateDto;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetAdminModelVariantListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelVariantDto;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import com.ksptool.ql.biz.service.ModelVariantService;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.enums.AiModelSeries;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@PrintLog
@RestController
@RequestMapping("/admin/model/variant")
public class AdminModelVariantController {

    @Autowired
    private ModelVariantService service;

    /**
     * 获取所有支持的AI模型系列
     * @return 模型系列代码列表
     */
    @PostMapping("getModelSeries")
    public Result<List<String>> getModelSeries() {
        // 从AiModelSeries枚举获取所有系列代码
        return Result.success(AiModelSeries.getAllCodes());
    }

    @PostMapping("toggleModelVariant")
    @RequirePermissionRest("admin:model:variant:save")
    public Result<String> toggleModelVariant(@RequestBody @Valid AdminToggleModelVariantDto dto) throws BizException {
        service.toggleModelVariant(dto);
        return Result.success("批量切换模型变体状态成功");
    }


    @PostMapping("getModelVariantList")
    @RequirePermissionRest("admin:model:variant:view")
    public Result<RestPageableView<GetAdminModelVariantListVo>> getModelVariantList(@RequestBody @Valid GetAdminModelVariantListDto dto){
        return Result.success(service.getModelVariantList(dto));
    }

    @PostMapping("getModelVariantDetails")
    @RequirePermissionRest("admin:model:variant:view")
    public Result<GetAdminModelVariantDetailsVo> getModelVariantDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getModelVariantDetails(dto.getId()));
    }

    @PostMapping("saveModelVariant")
    @RequirePermissionRest("admin:model:variant:save")
    public Result<String> saveModelVariant(@RequestBody @Valid SaveAdminModelVariantDto dto) throws BizException {
        service.saveModelVariant(dto);
        return Result.success("success");
    }

    @PostMapping("removeModelVariant")
    @RequirePermissionRest("admin:model:variant:remove")
    public Result<String> removeModelVariant(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeModelVariant(dto.getId());
        return Result.success("success");
    }

    /**
     * 应用参数模板到模型变体（支持批量应用和全局/个人参数选择）
     * @param dto 应用参数（包含模板ID、模型变体ID列表、应用范围）
     * @return 操作结果
     */
    @PostMapping("applyModelVariantParamTemplate")
    public Result<String> applyModelVariantParamTemplate(@RequestBody @Valid ApplyModelVariantParamTemplateDto dto) throws BizException {

        if (dto.getGlobal() == 0) {
            if (AuthService.hasPermission("admin:model:variant:param:template:apply:player")) {
                service.applyModelVariantParamTemplate(dto);
                return Result.success("应用模板为个人参数成功");
            }
            throw new BizException("权限不足：无法应用个人参数模板");
        }

        if (dto.getGlobal() == 1) {
            if (AuthService.hasPermission("admin:model:variant:param:template:apply:global")) {
                service.applyModelVariantParamTemplate(dto);
                return Result.success("应用模板为全局默认参数成功");
            }
            throw new BizException("权限不足：无法应用全局参数模板");
        }

        return Result.error("应用模板失败 未知的操作符:"+dto.getGlobal());
    }

}
