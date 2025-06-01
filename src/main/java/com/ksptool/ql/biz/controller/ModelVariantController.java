package com.ksptool.ql.biz.controller;


import com.ksptool.ql.biz.model.vo.GetModelVariantListVo;
import com.ksptool.ql.biz.service.ModelVariantService;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/model/variant")
public class ModelVariantController {

    @Autowired
    private ModelVariantService service;

    @RequestMapping("/getModelVariantList")
    public Result<List<GetModelVariantListVo>> getModelVariantList() {
        return Result.success(service.getClientModelVariantList());
    }

}
