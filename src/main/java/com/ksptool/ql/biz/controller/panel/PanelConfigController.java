package com.ksptool.ql.biz.controller.panel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/panel/config")
public class PanelConfigController {

    @GetMapping("/list")
    public ModelAndView getListView() {
        ModelAndView mv = new ModelAndView("panel-config-list");
        return mv;
    }
} 