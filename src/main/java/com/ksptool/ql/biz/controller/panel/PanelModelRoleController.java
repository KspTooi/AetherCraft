package com.ksptool.ql.biz.controller.panel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 模型角色管理控制器
 * 用于管理AI模型的角色信息和对话设置
 */
@Controller
@RequestMapping("/panel/model/role")
public class PanelModelRoleController {

    /**
     * 获取模型角色列表视图
     * @param model 模型
     * @return 模型角色列表视图
     */
    @GetMapping("/list")
    public ModelAndView getListView(Model model) {
        model.addAttribute("title", "模型角色管理");
        return new ModelAndView("panel-model-role");
    }
} 