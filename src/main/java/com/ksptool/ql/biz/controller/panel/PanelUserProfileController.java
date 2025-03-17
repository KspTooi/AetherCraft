package com.ksptool.ql.biz.controller.panel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户个人资料控制器
 */
@Controller
@RequestMapping("/panel/profile")
public class PanelUserProfileController {

    /**
     * 个人资料页面
     */
    @GetMapping("/view")
    public ModelAndView getView() {
        ModelAndView mav = new ModelAndView("panel-user-profile");
        return mav;
    }
} 