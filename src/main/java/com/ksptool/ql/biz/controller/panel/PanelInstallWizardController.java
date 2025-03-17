package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.web.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/install-wizard")
@RequiredArgsConstructor
public class PanelInstallWizardController {

    private final GlobalConfigService globalConfigService;
    
    /**
     * 检查是否需要显示安装向导
     * 如果allow.install.wizard配置为true，则显示安装向导
     * 否则重定向到登录页面
     */
    @GetMapping({"", "/", "/index"})
    public ModelAndView index() {
        // 检查是否需要显示安装向导
        String allowInstallWizard = globalConfigService.getValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey());
        
        // 如果配置不存在或为false，则重定向到登录页面
        if (StringUtils.isBlank(allowInstallWizard) || "false".equals(allowInstallWizard)) {
            return new ModelAndView("redirect:/login");
        }
        
        // 返回安装向导首页
        return new ModelAndView("install-wizard-index");
    }
    
    /**
     * 数据初始化页面
     */
    @GetMapping("/data-init")
    public ModelAndView dataInit() {
        // 检查是否需要显示安装向导
        String allowInstallWizard = globalConfigService.getValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey());
        
        // 如果配置不存在或为false，则重定向到登录页面
        if (StringUtils.isBlank(allowInstallWizard) || "false".equals(allowInstallWizard)) {
            return new ModelAndView("redirect:/login");
        }
        
        // 返回数据初始化页面
        return new ModelAndView("install-wizard-data-init");
    }
    
    /**
     * 安装完成页面
     */
    @GetMapping("/finish")
    public ModelAndView finish() {
        // 检查是否需要显示安装向导
        String allowInstallWizard = globalConfigService.getValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey());
        
        // 如果配置不存在或为false，则重定向到登录页面
        if (StringUtils.isBlank(allowInstallWizard) || "false".equals(allowInstallWizard)) {
            return new ModelAndView("redirect:/login");
        }
        
        // 返回安装完成页面
        return new ModelAndView("install-wizard-finish");
    }
    
    /**
     * 完成安装，设置allow.install.wizard为false
     */
    @PostMapping("/complete")
    @ResponseBody
    public Result<String> complete() {
        try {
            // 设置allow.install.wizard为false，表示安装向导已完成
            globalConfigService.setValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey(), "false");
            return Result.success("安装完成");
        } catch (Exception e) {
            return Result.error("完成安装失败：" + e.getMessage());
        }
    }
}
