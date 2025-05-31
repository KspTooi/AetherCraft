package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetAdminModelSeriesListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelSeriesDto;
import com.ksptool.ql.biz.model.vo.GetAdminModelSeriesDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelSeriesListVo;
import com.ksptool.ql.biz.service.ModelSeriesService;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/model/series")
public class AdminModelSeriesController {

    @Autowired
    private ModelSeriesService service;

    @PostMapping("getModelSeriesList")
    //@RequirePermissionRest("admin:model:series:view")
    public Result<RestPageableView<GetAdminModelSeriesListVo>> getModelSeriesList(@RequestBody @Valid GetAdminModelSeriesListDto dto){
        return Result.success(service.getModelSeriesList(dto));
    }

    @PostMapping("getModelSeriesDetails")
    //@RequirePermissionRest("admin:model:series:view")
    public Result<GetAdminModelSeriesDetailsVo> getModelSeriesDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getModelSeriesDetails(dto.getId()));
    }

    @PostMapping("saveModelSeries")
    //@RequirePermissionRest("admin:model:series:save")
    public Result<String> saveModelSeries(@RequestBody @Valid SaveAdminModelSeriesDto dto) throws BizException {
        service.saveModelSeries(dto);
        return Result.success("success");
    }

    @PostMapping("removeModelSeries")
    //@RequirePermissionRest("admin:model:series:delete")
    public Result<String> removeModelSeries(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeModelSeries(dto.getId());
        return Result.success("success");
    }

}
