package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.service.ModelRpService;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/model/rp")
public class ModelRpController {


    @Autowired
    private ModelRpService modelRpService;

    @PostMapping("/getRoleList")
    public Result<PageableView<GetModelRoleListVo>> getModelRoleList(@RequestBody @Valid GetModelRoleListDto queryDto) {
        return Result.success(modelRpService.getModelRoleList(queryDto));
    }


} 