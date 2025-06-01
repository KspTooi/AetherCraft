package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.biz.model.vo.ValidateSystemPermissionsVo;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.ModelVariantService;
import com.ksptool.ql.biz.service.UserService;
import com.ksptool.ql.biz.service.admin.AdminGroupService;
import com.ksptool.ql.biz.service.admin.AdminPermissionService;
import com.ksptool.ql.biz.service.panel.PanelInstallWizardService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/install-wizard")
public class AdminInstallWizardController {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private AdminPermissionService adminPermissionService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private PanelInstallWizardService service;

    @Autowired
    private AdminGroupService adminGroupService;

    @Autowired
    private ModelVariantService modelVariantService;

    /**
     * 检查是否需要显示安装向导
     * 如果allow.install.wizard配置为true，则显示安装向导
     * 否则重定向到登录页面
     */
    @GetMapping({"", "/", "/index"})
    public ModelAndView index() {

        // 检查是否需要显示安装向导 如果配置不存在或为false，则重定向到登录页面
        if (!service.hasInstallWizardMode()) {
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
        if (!service.hasInstallWizardMode()) {
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
        if (!service.hasInstallWizardMode()) {
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

        // 检查是否需要显示安装向导
        if (!service.hasInstallWizardMode()) {
            return Result.error("当前不在向导模式!");
        }

        try {
            // 设置allow.install.wizard为false，表示安装向导已完成
            globalConfigService.setValue(GlobalConfigEnum.ALLOW_INSTALL_WIZARD.getKey(), "false");
            globalConfigService.setValue(GlobalConfigEnum.APPLICATION_VERSION.getKey(), AetherLauncher.getVersion());
            return Result.success("安装完成");
        } catch (Exception e) {
            return Result.error("完成安装失败：" + e.getMessage());
        }
    }

    @PostMapping("/dataInitOrUpdate")
    @ResponseBody
    public Result<List<String>> dataInitOrUpdate() {

        // 检查是否需要显示安装向导
        if (!service.hasInstallWizardMode()) {
            return Result.error("当前不在向导模式!");
        }

        List<String> results = new ArrayList<>();
        
        try {
            // 1. 校验系统内置权限节点
            ValidateSystemPermissionsVo permissionResult = adminPermissionService.validateSystemPermissions();
            String permissionMessage;
            if (permissionResult.getAddedCount() > 0) {
                permissionMessage = String.format("权限节点校验完成，已添加 %d 个缺失的权限节点，已存在 %d 个权限节点", 
                        permissionResult.getAddedCount(), permissionResult.getExistCount());
            } else {
                permissionMessage = String.format("权限节点校验完成，所有 %d 个系统权限节点均已存在", 
                        permissionResult.getExistCount());
            }
            results.add(permissionMessage);
            
            // 2. 校验系统内置用户组
            String groupResult = adminGroupService.validateSystemGroups();
            results.add(groupResult);
            
            // 3. 校验系统内置用户
            String userResult = userService.validateSystemUsers();
            results.add(userResult);
            
            // 4. 校验系统全局配置项
            String configResult = globalConfigService.validateSystemConfigs();
            results.add(configResult);
            
            // 5. 校验系统内置模型变体
            String modelVariantResult = modelVariantService.validateSystemModelVariants();
            results.add(modelVariantResult);
            
            // 6. 更新应用版本号
            globalConfigService.setValue(GlobalConfigEnum.APPLICATION_VERSION.getKey(), AetherLauncher.getVersion());
            results.add("应用版本已更新为：" + AetherLauncher.getVersion());
            
            return Result.success("系统数据初始化完成", results);
        } catch (Exception e) {
            results.add("系统数据初始化过程中发生错误：" + e.getMessage());
            return Result.error("系统数据初始化失败");
        }
    }
}
