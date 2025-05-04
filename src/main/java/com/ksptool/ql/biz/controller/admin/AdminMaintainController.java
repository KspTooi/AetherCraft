package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.vo.ValidateSystemPermissionsVo;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.UserService;
import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 维护工具控制器
 */
@Controller
@RequestMapping("/admin/maintain")
public class AdminMaintainController {

    @Autowired
    private PanelPermissionService panelPermissionService;

    @Autowired
    private PanelGroupService panelGroupService;

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalConfigService globalConfigService;
    
    /**
     * 校验系统内置权限节点
     * 检查数据库中是否存在所有系统内置权限，如果不存在则自动创建
     */
    @PostMapping("/validSystemPermission")
    @ResponseBody
    @RequirePermissionRest("panel:maintain:permission")
    public Result<ValidateSystemPermissionsVo> validateSystemPermissions() {
        try {
            ValidateSystemPermissionsVo result = panelPermissionService.validateSystemPermissions();
            
            String message;
            if (result.getAddedCount() > 0) {
                message = String.format("校验完成，已添加 %d 个缺失的权限节点，已存在 %d 个权限节点", 
                        result.getAddedCount(), result.getExistCount());
            } else {
                message = String.format("校验完成，所有 %d 个系统权限节点均已存在", result.getExistCount());
            }
            
            return Result.success(message, result);
        } catch (Exception e) {
            return Result.error("校验权限节点失败：" + e.getMessage());
        }
    }

    /**
     * 校验系统内置用户组
     * 检查数据库中是否存在所有系统内置用户组，如果不存在则自动创建
     */
    @PostMapping("/validSystemGroup")
    @ResponseBody
    @RequirePermissionRest("panel:maintain:permission")
    public Result<String> validateSystemGroups() {
        try {
            String result = panelGroupService.validateSystemGroups();
            return Result.success(result,null);
        } catch (Exception e) {
            return Result.error("校验用户组失败：" + e.getMessage());
        }
    }

    /**
     * 校验系统内置用户
     * 检查数据库中是否存在所有系统内置用户，如果不存在则自动创建
     */
    @PostMapping("/validSystemUsers")
    @ResponseBody
    @RequirePermissionRest("panel:maintain:permission")
    public Result<String> validateSystemUsers() {
        try {
            String result = userService.validateSystemUsers();
            return Result.success(result,null);
        } catch (Exception e) {
            return Result.error("校验用户失败：" + e.getMessage());
        }
    }

    /**
     * 校验系统全局配置项
     * 检查数据库中是否存在所有系统内置的全局配置项，如果不存在则自动创建
     */
    @PostMapping("/validSystemConfigs")
    @ResponseBody
    @RequirePermissionRest("panel:maintain:permission")
    public Result<String> validateSystemConfigs() {
        try {
            String result = globalConfigService.validateSystemConfigs();
            return Result.success(result, null);
        } catch (Exception e) {
            return Result.error("校验系统配置项失败：" + e.getMessage());
        }
    }
} 