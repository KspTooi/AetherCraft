package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamTemplateValueListDto;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamTemplateValueDetailsDto;
import com.ksptool.ql.biz.model.dto.SaveModelVariantParamTemplateValueDto;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamTemplateValueVo;
import com.ksptool.ql.biz.service.admin.AdminModelVariantParamTemplateValueService;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/model/variant/param/template/value")
public class AdminModelVariantParamTemplateValueController {

    @Autowired
    private AdminModelVariantParamTemplateValueService service;

    /**
     * 查询指定模板的参数值列表（分页）
     * @param dto 查询参数
     * @return 模板值列表
     */
    @PostMapping("getModelVariantParamTemplateValueList")
    @RequirePermissionRest("admin:model:variant:param:template:value:view")
    public Result<RestPageableView<GetModelVariantParamTemplateValueVo>> getModelVariantParamTemplateValueList(@RequestBody @Valid GetModelVariantParamTemplateValueListDto dto) {
        try {
            return Result.success(service.getModelVariantParamTemplateValueList(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询模板参数值详情
     * @param dto 查询参数
     * @return 模板参数值详情
     */
    @PostMapping("getModelVariantParamTemplateValueDetails")
    @RequirePermissionRest("admin:model:variant:param:template:value:view")
    public Result<GetModelVariantParamTemplateValueVo> getModelVariantParamTemplateValueDetails(@RequestBody @Valid GetModelVariantParamTemplateValueDetailsDto dto) {
        try {
            return Result.success(service.getModelVariantParamTemplateValueDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 新增或编辑单个模板参数值
     * @param dto 保存参数
     * @return 操作结果
     */
    @PostMapping("saveModelVariantParamTemplateValue")
    @RequirePermissionRest("admin:model:variant:param:template:value:save")
    public Result<String> saveModelVariantParamTemplateValue(@RequestBody @Valid SaveModelVariantParamTemplateValueDto dto) {
        try {
            service.saveModelVariantParamTemplateValue(dto);
            return Result.success("保存模板参数值成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除单个模板参数值
     * @param dto 删除参数
     * @return 操作结果
     */
    @PostMapping("removeModelVariantParamTemplateValue")
    @RequirePermissionRest("admin:model:variant:param:template:value:delete")
    public Result<String> removeModelVariantParamTemplateValue(@RequestBody @Valid CommonIdDto dto) {
        try {
            service.removeModelVariantParamTemplateValue(dto);
            return Result.success("删除模板参数值成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }
} 