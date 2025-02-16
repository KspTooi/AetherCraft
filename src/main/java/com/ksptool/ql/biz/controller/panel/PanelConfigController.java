package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListPanelConfigDto;
import com.ksptool.ql.biz.service.panel.PanelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/panel/config")
public class PanelConfigController {

    @Autowired
    private PanelConfigService panelConfigService;

    /**
     * 配置项列表页面
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListPanelConfigDto dto) {
        ModelAndView mv = new ModelAndView("panel-config-list");
        mv.addObject("data", panelConfigService.getListView(dto));
        mv.addObject("query", dto);
        return mv;
    }
} 