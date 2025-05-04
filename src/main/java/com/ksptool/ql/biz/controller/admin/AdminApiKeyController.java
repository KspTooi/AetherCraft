package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetApiKeyListDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.vo.GetApiKeyDetailsVo;
import com.ksptool.ql.biz.model.vo.GetApiKeyListVo;
import com.ksptool.ql.biz.service.admin.AdminApiKeyService;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import com.ksptool.ql.commons.exception.BizException;

@RestController
@RequestMapping("/admin/apikey")
public class AdminApiKeyController {

    @Autowired
    private AdminApiKeyService service;

    @PostMapping("getApiKeyList")
    public Result<RestPageableView<GetApiKeyListVo>> getApiKeyList(@RequestBody @Valid GetApiKeyListDto dto){
        return Result.success(service.getApiKeyList(dto));
    }

    @PostMapping("getApiKeyDetails")
    public Result<GetApiKeyDetailsVo> getApiKeyDetails(@RequestBody @Valid CommonIdDto dto){
        try {
            return Result.success(service.getApiKeyDetails(dto));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("saveApiKey")
    public Result<String> saveApiKey(@RequestBody @Valid SaveApiKeyDto dto){
        try {
            service.saveApiKey(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeApiKey")
    public Result<String> removeApiKey(@RequestBody @Valid CommonIdDto dto){
        try {
            service.removeApiKey(dto);
            return Result.success("success");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }





}
