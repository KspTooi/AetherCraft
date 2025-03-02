package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import com.ksptool.ql.biz.model.dto.SaveModelUserRoleDto;
import com.ksptool.ql.biz.model.vo.ListModelUserRoleVo;
import com.ksptool.ql.biz.model.vo.ModelUserRoleVo;
import com.ksptool.ql.biz.service.ModelUserRoleService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    
    @Autowired
    private UserFileService userFileService;

    /**
     * 获取用户角色列表视图
     * @param queryDto 查询参数
     * @param model Spring MVC Model对象
     * @return ModelAndView
     */
    @GetMapping("/list")
    public ModelAndView getListView(ModelUserRoleQueryDto queryDto, org.springframework.ui.Model model) {
        ModelAndView modelAndView = new ModelAndView("panel-model-user-role");
        
        // 创建响应数据对象
        ListModelUserRoleVo listVo = new ListModelUserRoleVo();
        
        // 查询角色列表
        List<ModelUserRoleVo> roles = modelUserRoleService.findRoles(queryDto);
        listVo.setRoles(roles);
        
        // 查询当前选中的角色
        ModelUserRoleVo selectedRole = null;
        
        // 如果是新建角色模式
        if (queryDto.getIsNewRole() != null && queryDto.getIsNewRole()) {
            // 创建一个空的角色对象
            selectedRole = new ModelUserRoleVo();
            // 设置默认值
            selectedRole.setSortOrder(0);
            selectedRole.setIsDefault(0);
            
            // 标记为新建角色
            listVo.setIsNewRole(true);
            
            // 检查是否有表单数据需要回显
            SaveModelUserRoleDto formData = (SaveModelUserRoleDto) model.asMap().get("formData");
            if (formData != null) {
                // 使用表单数据填充selectedRole
                selectedRole.setName(formData.getName());
                selectedRole.setDescription(formData.getDescription());
                selectedRole.setSortOrder(formData.getSortOrder());
                selectedRole.setIsDefault(formData.getIsDefault());
                selectedRole.setAvatarPath(formData.getAvatarPath());
            }
        }
        
        // 如果指定了选中角色ID
        if (queryDto.getSelectedId() != null) {
            selectedRole = modelUserRoleService.findById(queryDto.getSelectedId());
            
            // 检查是否有表单数据需要回显
            SaveModelUserRoleDto formData = (SaveModelUserRoleDto) model.asMap().get("formData");
            if (formData != null && formData.getId() != null && formData.getId().equals(queryDto.getSelectedId())) {
                // 使用表单数据更新selectedRole
                selectedRole.setName(formData.getName());
                selectedRole.setDescription(formData.getDescription());
                selectedRole.setSortOrder(formData.getSortOrder());
                selectedRole.setIsDefault(formData.getIsDefault());
                selectedRole.setAvatarPath(formData.getAvatarPath());
            }
        }
        
        // 如果没有选中角色且角色列表不为空，默认选中第一个
        if (selectedRole == null && !roles.isEmpty()) {
            selectedRole = roles.get(0);
        }
        
        listVo.setSelectedRole(selectedRole);
        listVo.setQuery(queryDto);
        
        // 将响应数据放入 ModelAndView
        modelAndView.addObject("data", listVo);
        return modelAndView;
    }
    
    /**
     * 获取角色添加页面
     * @return String 重定向到列表页
     */
    @GetMapping("/add")
    public String addView() {
        // 重定向到列表页，并传递 isNewRole 参数
        return "redirect:/panel/model/user-role/list?isNewRole=true";
    }
    
    /**
     * 保存角色
     * @param roleDto 角色信息
     * @param bindingResult 表单验证结果
     * @param ra 重定向属性
     * @return 重定向到列表页
     */
    @PostMapping("/save")
    public String saveModelUserRole(@Valid SaveModelUserRoleDto roleDto, BindingResult bindingResult, RedirectAttributes ra) {
       
        // 处理表单验证错误
        if (bindingResult.hasErrors()) {
            // 获取第一个错误信息
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("vo", Result.error(errorMessage));
            ra.addFlashAttribute("formData", roleDto);

            // 新建角色模式
            if (roleDto.getId() == null) {
                return "redirect:/panel/model/user-role/list?isNewRole=true";
            }
            
            // 编辑角色模式
            return "redirect:/panel/model/user-role/list?selectedId=" + roleDto.getId();
        }
        
        try {
            // 保存角色，获取保存后的角色ID
            Long savedRoleId = modelUserRoleService.saveModelUserRole(roleDto);
            
            // 设置成功消息
            ra.addFlashAttribute("vo", Result.success("角色保存成功", null));
            
            // 重定向到列表页，并选中保存的角色
            return "redirect:/panel/model/user-role/list?selectedId=" + savedRoleId;
        } catch (Exception e) {
            // 设置错误消息
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            ra.addFlashAttribute("formData", roleDto);

            // 新建角色模式
            if (roleDto.getId() == null) {
                return "redirect:/panel/model/user-role/list?isNewRole=true";
            }

            // 编辑角色模式
            return "redirect:/panel/model/user-role/list?selectedId=" + roleDto.getId();
        }
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到列表页
     */
    @GetMapping("/delete")
    public String removeModelUserRole(@RequestParam(name = "id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // 删除角色
            modelUserRoleService.removeModelUserRole(id);
            redirectAttributes.addFlashAttribute("vo", Result.success("角色删除成功", null));
        } catch (Exception e) {
            // 设置业务异常消息
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
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
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 使用统一文件服务保存头像
            var userFile = userFileService.receive(file);
            return Result.success(userFile.getFilepath());
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
} 