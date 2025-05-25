package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.vo.GetModelSeriesVo;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.enums.AIModelEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/model/series")
public class ModelSeriesController {

    @PostMapping("getModelSeries")
    public Result<List<GetModelSeriesVo>> getModelSeries() {
        List<GetModelSeriesVo> seriesList = new ArrayList<>();
        
        // 遍历所有模型枚举
        for (AIModelEnum model : AIModelEnum.values()) {
            GetModelSeriesVo vo = new GetModelSeriesVo();
            vo.setModelCode(model.getCode());
            vo.setModelName(model.getName());
            vo.setSeries(model.getSeries());
            vo.setSize(model.getSize());
            vo.setSpeed(model.getSpeed());
            vo.setIntelligence(model.getIntelligence());
            vo.setThinking(model.getThinking());
            seriesList.add(vo);
        }
        
        return Result.success(seriesList);
    }

}
