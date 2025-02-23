package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.vo.ModelConfigVo;
import com.ksptool.ql.commons.enums.AIModelEnum;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/panel/model")
public class PanelModelConfigController {

    @GetMapping("/edit")
    public ModelAndView editPage(@RequestParam(name = "model", required = false) String model) {
        ModelAndView mv = new ModelAndView("panel-model-config");
        // 设置页面标题
        mv.addObject("title", "AI模型配置");

        // 创建配置VO
        ModelConfigVo config = new ModelConfigVo();

        // 获取目标模型
        AIModelEnum modelEnum = null;
        if (model != null) {
            modelEnum = AIModelEnum.getByCode(model);
        }

        // 如果模型未指定或无效，使用第一个模型
        if (modelEnum == null) {
            modelEnum = AIModelEnum.values()[0];
        }

        // 设置模型信息
        config.setModel(modelEnum.getCode());
        config.setModelName(modelEnum.getName());
        // TODO: 从配置服务加载其他配置

        mv.addObject("data", config);
        return mv;
    }
} 