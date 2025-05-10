package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.vo.GetApiKeyAuthorizationDetailsVo;
import com.ksptool.ql.biz.model.vo.GetApiKeyAuthorizationListVo;
import com.ksptool.ql.biz.model.vo.GetApiKeyDetailsVo;
import com.ksptool.ql.biz.model.vo.GetApiKeyListVo;
import com.ksptool.ql.biz.service.admin.AdminApiKeyService;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.AIModelEnum;
import java.util.List;

@RestController
@RequestMapping("/admin/apikey")
public class AdminApiKeyController {

    @Autowired
    private AdminApiKeyService service;

    @PostMapping("getSeriesList")
    public Result<List<String>> getSeriesList(){
        return Result.success(AIModelEnum.getSeriesList());
    }

    @PostMapping("getApiKeyList")
    @RequirePermissionRest("admin:apikey:view")
    public Result<RestPageableView<GetApiKeyListVo>> getApiKeyList(@RequestBody @Valid GetApiKeyListDto dto){
        return Result.success(service.getApiKeyList(dto));
    }

    @PostMapping("getApiKeyDetails")
    @RequirePermissionRest("admin:apikey:save")
    public Result<GetApiKeyDetailsVo> getApiKeyDetails(@RequestBody @Valid CommonIdDto dto){
        try {
            return Result.success(service.getApiKeyDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("saveApiKey")
    @RequirePermissionRest("admin:apikey:save")
    public Result<String> saveApiKey(@RequestBody @Valid SaveApiKeyDto dto){
        try {

            if(dto.getId() == null){
                if(StringUtils.isBlank(dto.getKeyValue())){
                    return Result.error("创建密钥时密钥值不可为空");
                }
            }

            service.saveApiKey(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeApiKey")
    @RequirePermissionRest("admin:apikey:delete")
    public Result<String> removeApiKey(@RequestBody @Valid CommonIdDto dto){
        try {
            service.removeApiKey(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    //获取某个Apikey的授权列表
    @PostMapping("getAuthorizationList")
    @RequirePermissionRest("admin:apikey:auth:view")
    public Result<RestPageableView<GetApiKeyAuthorizationListVo>> getAuthorizationList(@RequestBody @Valid GetApiKeyAuthorizationListDto dto){
        try{
            return Result.success(service.getAuthorizationList(dto));
        }catch (BizException e){
            return Result.error(e.getMessage());
        }
    }

    //查询授权详情
    @PostMapping("getApiKeyAuthorizationDetails")
    @RequirePermissionRest("admin:apikey:auth:save")
    public Result<GetApiKeyAuthorizationDetailsVo> getApiKeyAuthorizationDetails(@RequestBody @Valid CommonIdDto dto){
        try {
            return Result.success(service.getApiKeyAuthorizationDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //创建Apikey的授权
    @PostMapping("saveAuth")
    @RequirePermissionRest("admin:apikey:auth:save")
    public Result<String> saveAuth(@RequestBody @Valid SaveApiKeyAuthorizationDto dto){
        try {
            service.saveAuth(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    //移除授权关系
    @PostMapping("removeAuthorization")
    @RequirePermissionRest("admin:apikey:auth:remove")
    public Result<String> removeAuthorization(@RequestBody @Valid CommonIdDto dto){
        try {
            service.removeAuthorization(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }




}