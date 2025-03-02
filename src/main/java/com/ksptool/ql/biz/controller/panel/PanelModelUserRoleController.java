package com.ksptool.ql.biz.controller.panel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户角色管理控制器
 */
@Controller
@RequestMapping("/panel/model/user-role")
public class PanelModelUserRoleController {

    /**
     * 获取用户角色列表视图
     * @return ModelAndView
     */
    @GetMapping("/list")
    public ModelAndView getListView() {
        ModelAndView modelAndView = new ModelAndView("panel-model-user-role");
        // 暂时不添加数据，仅返回视图
        return modelAndView;
    }
} 