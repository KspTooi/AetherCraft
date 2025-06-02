package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.vo.*;
import com.ksptool.ql.biz.service.admin.AdminModelVariantParamTemplateService;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/model/variant/param/template")
public class AdminModelVariantParamTemplateController {

    @Autowired
    private AdminModelVariantParamTemplateService service;

    /**
     * 查询当前用户的参数模板列表（不包含模板值）
     * @param dto 查询参数
     * @return 模板列表
     */
    @PostMapping("getModelVariantParamTemplateList")
    @RequirePermissionRest("admin:model:variant:param:template:view")
    public Result<RestPageableView<GetModelVariantParamTemplateListVo>> getModelVariantParamTemplateList(@RequestBody @Valid GetModelVariantParamTemplateListDto dto) {
        try {
            return Result.success(service.getModelVariantParamTemplateList(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户的模板详情（不包含模板值，模板值由另一个控制器管理）
     * @param dto 查询参数
     * @return 模板详情
     */
    @PostMapping("getModelVariantParamTemplateDetails")
    @RequirePermissionRest("admin:model:variant:param:template:view")
    public Result<GetModelVariantParamTemplateDetailsVo> getModelVariantParamTemplateDetails(@RequestBody @Valid GetModelVariantParamTemplateDetailsDto dto) throws BizException {
        try {
            return Result.success(service.getModelVariantParamTemplateDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存当前用户的参数模板（仅模板基本信息，不包含模板值）
     * @param dto 保存参数
     * @return 操作结果
     */
    @PostMapping("saveModelVariantParamTemplate")
    @RequirePermissionRest("admin:model:variant:param:template:save")
    public Result<String> saveModelVariantParamTemplate(@RequestBody @Valid SaveModelVariantParamTemplateDto dto) throws BizException {
        try {
            service.saveModelVariantParamTemplate(dto);
            return Result.success("保存模板成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除当前用户的参数模板
     * @param dto 删除参数
     * @return 操作结果
     */
    @PostMapping("removeModelVariantParamTemplate")
    @RequirePermissionRest("admin:model:variant:param:template:delete")
    public Result<String> removeModelVariantParamTemplate(@RequestBody @Valid CommonIdDto dto) throws BizException {
        try {
            service.removeModelVariantParamTemplate(dto);
            return Result.success("删除模板成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

} 