package com.ksptool.ql.biz.controller.panel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 维护工具控制器
 */
@Controller
@RequestMapping("/panel/maintain")
public class PanelMaintainController {

    /**
     * 维护工具页面
     */
    @GetMapping("/view")
    public ModelAndView view() {
        ModelAndView mav = new ModelAndView("panel-maintain");
        return mav;
    }
} 