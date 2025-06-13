package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.GetModelVariantParamListDto;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamDetailsDto;
import com.ksptool.ql.biz.model.dto.RemoveModelVariantParamDto;
import com.ksptool.ql.biz.model.dto.SaveModelVariantParamDto;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamDetailsVo;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamListVo;
import com.ksptool.ql.biz.service.admin.AdminModelVariantParamService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.biz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/admin/model/variant/param")
public class AdminModelVariantParamController {

    @Autowired
    private AdminModelVariantParamService service;

    /**
     * 查询模型变体参数列表，VO中包含全局值和我的值
     * @param dto 查询参数
     * @return 参数列表
     */
    @PostMapping("getModelVariantParamList")
    @RequirePermissionRest("admin:model:variant:param:view")
    public Result<RestPageableView<GetModelVariantParamListVo>> getModelVariantParamList(@RequestBody @Valid GetModelVariantParamListDto dto) {
        try {
            return Result.success(service.getModelVariantParamList(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询参数详情
     * @param dto 查询参数
     * @return 参数详情
     */
    @PostMapping("getModelVariantParamDetails")
    @RequirePermissionRest("admin:model:variant:param:view")
    public Result<GetModelVariantParamDetailsVo> getModelVariantParamDetails(@RequestBody @Valid GetModelVariantParamDetailsDto dto) throws BizException {
        try {
            return Result.success(service.getModelVariantParamDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存参数配置，可保存全局或个人参数
     * @param dto 保存参数
     * @return 操作结果
     */
    @PostMapping("saveModelVariantParam")
    public Result<String> saveModelVariantParam(@RequestBody @Valid SaveModelVariantParamDto dto) throws BizException {
        if (dto.getGlobal() == 1) {
            if (AuthService.hasPermission("admin:model:variant:param:save:global")) {
                service.saveModelVariantParam(dto);
                return Result.success("保存参数配置成功");
            }
            throw new BizException("无权限操作全局参数");
        }

        if (dto.getGlobal() == 0) {
            if (AuthService.hasPermission("admin:model:variant:param:save:self")) {
                service.saveModelVariantParam(dto);
                return Result.success("保存参数配置成功");
            }
            throw new BizException("无权限操作个人参数");
        }

        return Result.error("保存参数配置失败 未知的操作符:" + dto.getGlobal());
    }

    /**
     * 删除参数配置
     * @param dto 删除参数
     * @return 操作结果
     */
    @PostMapping("removeModelVariantParam")
    public Result<String> removeModelVariantParam(@RequestBody @Valid RemoveModelVariantParamDto dto) throws BizException {
        if (dto.getGlobal() == 1) {
            if (AuthService.hasPermission("admin:model:variant:param:remove:global")) {
                service.removeModelVariantParam(dto);
                return Result.success("删除参数配置成功");
            }
            throw new BizException("无权限删除全局参数");
        }

        if (dto.getGlobal() == 0) {
            if (AuthService.hasPermission("admin:model:variant:param:remove:player")) {
                service.removeModelVariantParam(dto);
                return Result.success("删除参数配置成功");
            }
            throw new BizException("无权限删除个人参数");
        }

        return Result.error("删除参数配置失败 未知的操作符:" + dto.getGlobal());
    }

} 