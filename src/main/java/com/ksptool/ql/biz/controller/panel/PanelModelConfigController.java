package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.service.panel.PanelModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/panel/model")
public class PanelModelConfigController {
    
    @Autowired
    private PanelModelConfigService panelModelConfigService;
    
    @GetMapping("/edit")
    public ModelAndView editPage(@RequestParam(name = "model", required = false) String model) {
        ModelAndView mv = new ModelAndView("panel-model-config");
        // 设置页面标题
        mv.addObject("title", "AI模型配置");
        // 获取配置数据
        mv.addObject("data", panelModelConfigService.getEditView(model));
        return mv;
    }
} 