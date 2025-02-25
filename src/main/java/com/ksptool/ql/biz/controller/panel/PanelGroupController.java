package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.dto.SavePanelGroupDto;
import com.ksptool.ql.biz.model.dto.ListPanelGroupDto;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.biz.model.vo.SavePanelGroupVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户组管理控制器
 */
@Controller
@RequestMapping("/panel/group")
public class PanelGroupController {

    @Autowired
    private PanelGroupService groupService;

    /**
     * 用户组列表页面
     */
    @GetMapping("/list")
    @RequirePermission("panel:group:view")
    public ModelAndView list(@Valid ListPanelGroupDto dto) {
        ModelAndView mv = new ModelAndView("panel-group-manager");
        mv.addObject("data", groupService.getListView(dto));
        mv.addObject("query", dto);
        return mv;
    }

    /**
     * 创建用户组页面
     */
    @GetMapping("/create")
    @RequirePermission("panel:group:add")
    public ModelAndView create() {
        ModelAndView mv = new ModelAndView("panel-group-operator");
        mv.addObject("group", groupService.getCreateView());
        return mv;
    }

    /**
     * 编辑用户组页面
     */
    @GetMapping("/edit/{id}")
    @RequirePermission("panel:group:edit")
    public ModelAndView edit(@PathVariable(name = "id") Long id) {
        ModelAndView mv = new ModelAndView();
        
        try {
            SavePanelGroupVo vo = groupService.getEditView(id);
            mv.addObject("group", vo);
            mv.setViewName("panel-group-operator");
        } catch (BizException e) {
            mv.setViewName("redirect:/panel/group/list");
        }
        
        return mv;
    }

    /**
     * 保存用户组
     */
    @PostMapping("/save")
    @RequirePermission("panel:group:edit")
    public ModelAndView save(@Valid SavePanelGroupDto dto, BindingResult bindingResult, RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView();
        
        // 验证失败处理
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
            ra.addFlashAttribute("group", dto);
            mv.setViewName(dto.getId() == null ? "redirect:/panel/group/create" : "redirect:/panel/group/edit/" + dto.getId());
            return mv;
        }
        
        try {
            // 判断是否为创建模式
            boolean isCreate = dto.getId() == null;
            
            // 保存用户组
            groupService.savePanelGroup(dto);
            
            // 设置成功消息
            if (isCreate) {
                ra.addFlashAttribute("vo", Result.success(String.format("已创建用户组:%s", dto.getName()), null));
                // 创建模式：清空表单，返回创建页面继续创建
                mv.setViewName("redirect:/panel/group/create");
            } else {
                ra.addFlashAttribute("vo", Result.success(String.format("已更新用户组:%s", dto.getName()), null));
                // 编辑模式：返回列表页
                mv.setViewName("redirect:/panel/group/list");
            }
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            // 保持原有数据，返回对应页面
            ra.addFlashAttribute("group", dto);
            
            if (dto.getId() != null) {
                mv.setViewName("redirect:/panel/group/edit/" + dto.getId());
            } else {
                mv.setViewName("redirect:/panel/group/create");
            }
        }
        
        return mv;
    }

    /**
     * 删除用户组
     */
    @PostMapping("/remove/{id}")
    @RequirePermission("panel:group:delete")
    public ModelAndView remove(@PathVariable(name = "id") Long id, RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView("redirect:/panel/group/list");
        
        try {
            String groupName = groupService.removeGroup(id);
            ra.addFlashAttribute("vo", Result.success(String.format("已删除用户组:%s", groupName), null));
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mv;
    }
} 