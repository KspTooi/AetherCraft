package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetAdminModelVariantListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelVariantDto;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import com.ksptool.ql.biz.service.ModelVariantService;
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
        String[] seriesCodes = AiModelSeries.getAllCodes();
        List<String> seriesList = Arrays.asList(seriesCodes);
        
        return Result.success(seriesList);
    }

    @PostMapping("getModelVariantList")
    //@RequirePermissionRest("admin:model:variant:view")
    public Result<RestPageableView<GetAdminModelVariantListVo>> getModelVariantList(@RequestBody @Valid GetAdminModelVariantListDto dto){
        return Result.success(service.getModelVariantList(dto));
    }

    @PostMapping("getModelVariantDetails")
    //@RequirePermissionRest("admin:model:variant:view")
    public Result<GetAdminModelVariantDetailsVo> getModelVariantDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getModelVariantDetails(dto.getId()));
    }

    @PostMapping("saveModelVariant")
    //@RequirePermissionRest("admin:model:variant:save")
    public Result<String> saveModelVariant(@RequestBody @Valid SaveAdminModelVariantDto dto) throws BizException {
        service.saveModelVariant(dto);
        return Result.success("success");
    }

    @PostMapping("removeModelVariant")
    //@RequirePermissionRest("admin:model:variant:delete")
    public Result<String> removeModelVariant(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeModelVariant(dto.getId());
        return Result.success("success");
    }

}
