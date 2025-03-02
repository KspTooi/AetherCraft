package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.biz.model.vo.SaveModelRoleVo;
import com.ksptool.ql.biz.service.panel.PanelModelRoleService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.assign;

/**
 * 模型角色管理控制器
 * 用于管理AI模型的角色信息和对话设置
 */
@Controller
@RequestMapping("/panel/model/role")
@SessionAttributes("formData")
public class PanelModelRoleController {

    @Autowired
    private PanelModelRoleService panelModelRoleService;
    
    /**
     * 初始化表单数据
     */
    @ModelAttribute("formData")
    public SaveModelRoleDto initFormData() {
        return new SaveModelRoleDto();
    }

    /**
     * 获取模型角色列表视图
     * @param dto 查询参数
     * @return 模型角色列表视图
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListModelRoleDto dto, @ModelAttribute("formData") SaveModelRoleDto formData, SessionStatus sessionStatus, Model model) {
        // 调用服务获取视图数据
        ListModelRoleVo vo = panelModelRoleService.getListView(dto);
        
        // 如果是新建模式，设置isNew标志
        if (dto.getIsNew()) {
            ListModelRoleVo newVo = new ListModelRoleVo();
            newVo.setKeyword(dto.getKeyword());
            newVo.setRoleList(vo.getRoleList());
            newVo.setIsNew(true);
            newVo.setId(null);
            newVo.setStatus(1);
            vo = newVo;
        }
        
        // 检查是否有保存失败时的表单数据
        if (model.containsAttribute("hasFormError") && formData != null && formData.getName() != null) {
            // 将表单数据应用到视图对象
            vo.setIsNew(formData.getId() == null);
            assign(formData, vo);
        } else {
            // 清除会话中的表单数据
            sessionStatus.setComplete();
        }
        
        // 创建ModelAndView并设置数据
        ModelAndView mv = new ModelAndView("panel-model-role");
        mv.addObject("title", "模型角色管理");
        mv.addObject("data", vo);
        return mv;
    }
    
    /**
     * 保存模型角色
     * @param dto 保存参数
     * @return 保存结果视图
     */
    @PostMapping("/save")
    public String saveModelRole(@Valid @ModelAttribute("formData") SaveModelRoleDto dto, BindingResult br, RedirectAttributes ra, Model model) {
        // 表单验证
        if (br.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error(br.getAllErrors().get(0).getDefaultMessage()));
            ra.addFlashAttribute("hasFormError", true);
            return "redirect:/panel/model/role/list";
        }
        
        try {
            // 调用服务保存角色
            SaveModelRoleVo vo = panelModelRoleService.saveModelRole(dto);
            
            if (!vo.getSuccess()) {
                throw new BizException(vo.getMessage());
            }
            
            // 保存成功，添加成功消息
            ra.addFlashAttribute("vo", Result.success(vo.getMessage(), vo));
            
            // 重定向到列表页面，并选中刚保存的角色
            return "redirect:/panel/model/role/list?id=" + vo.getId();
        } catch (Exception e) {
            // 异常处理
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            // 标记有表单错误
            ra.addFlashAttribute("hasFormError", true);
            return "redirect:/panel/model/role/list";
        }
    }
}