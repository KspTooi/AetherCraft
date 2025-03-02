package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import com.ksptool.ql.biz.model.vo.ModelUserRoleVo;
import com.ksptool.ql.biz.service.ModelUserRoleService;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 用户角色管理控制器
 */
@Controller
@RequestMapping("/panel/model/user-role")
public class PanelModelUserRoleController {

    @Autowired
    private ModelUserRoleService modelUserRoleService;

    /**
     * 获取用户角色列表视图
     * @param queryDto 查询参数
     * @return ModelAndView
     */
    @GetMapping("/list")
    public ModelAndView getListView(ModelUserRoleQueryDto queryDto) {
        ModelAndView modelAndView = new ModelAndView("panel-model-user-role");
        
        // 查询角色列表
        List<ModelUserRoleVo> roles = modelUserRoleService.findRoles(queryDto);
        modelAndView.addObject("roles", roles);
        
        // 查询当前选中的角色
        ModelUserRoleVo selectedRole = null;
        if (queryDto.getSelectedId() != null) {
            selectedRole = modelUserRoleService.findById(queryDto.getSelectedId());
        } else if (!roles.isEmpty()) {
            // 如果没有指定选中角色，默认选中第一个
            selectedRole = roles.get(0);
        }
        
        modelAndView.addObject("selectedRole", selectedRole);
        modelAndView.addObject("query", queryDto);
        return modelAndView;
    }
    
    /**
     * 获取角色添加页面
     * @return ModelAndView
     */
    @GetMapping("/add")
    public ModelAndView addView() {
        ModelAndView modelAndView = new ModelAndView("panel-model-user-role-add");
        // 创建一个空的角色对象
        ModelUserRoleVo roleVo = new ModelUserRoleVo();
        // 设置默认值
        roleVo.setSortOrder(0);
        roleVo.setIsDefault(0);
        modelAndView.addObject("role", roleVo);
        return modelAndView;
    }
    
    /**
     * 保存角色
     * @param roleVo 角色信息
     * @param redirectAttributes 重定向属性
     * @return 重定向到列表页
     */
    @PostMapping("/save")
    public String saveModelUserRole(@RequestBody ModelUserRoleVo roleVo, RedirectAttributes redirectAttributes) {
        try {
            // 保存角色
            ModelUserRoleVo savedRole = modelUserRoleService.saveRole(roleVo);
            
            // 设置成功消息
            redirectAttributes.addFlashAttribute("vo", Result.success("角色保存成功", null));
            
            // 重定向到列表页，并选中保存的角色
            return "redirect:/panel/model/user-role/list?selectedId=" + savedRole.getId();
        } catch (Exception e) {
            // 设置错误消息
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
            
            // 重定向到列表页
            return "redirect:/panel/model/user-role/list";
        }
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到列表页
     */
    @GetMapping("/delete")
    public String deleteModelUserRole(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            // 删除角色
            boolean success = modelUserRoleService.deleteRole(id);
            
            if (success) {
                // 设置成功消息
                redirectAttributes.addFlashAttribute("vo", Result.success("角色删除成功", null));
            } else {
                // 设置错误消息
                redirectAttributes.addFlashAttribute("vo", Result.error("无法删除默认角色"));
            }
        } catch (Exception e) {
            // 设置错误消息
            redirectAttributes.addFlashAttribute("vo", Result.error("角色删除失败：" + e.getMessage()));
        }
        
        // 重定向到列表页
        return "redirect:/panel/model/user-role/list";
    }
    
    /**
     * 上传角色头像
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/upload-avatar")
    @ResponseBody
    public Result<String> uploadAvatar(@RequestParam("file") Object file) {
        // TODO: 实现文件上传逻辑
        // 这里需要实现文件上传的逻辑，返回上传后的文件路径
        
        // 模拟上传成功
        return Result.success("/static/img/avatar-default.jpg");
    }
} 