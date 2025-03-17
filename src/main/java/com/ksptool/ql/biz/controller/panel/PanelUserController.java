package com.ksptool.ql.biz.controller.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.model.dto.ListPanelUserDto;
import com.ksptool.ql.biz.model.dto.SavePanelUserDto;
import com.ksptool.ql.biz.model.vo.SavePanelUserGroupVo;
import com.ksptool.ql.biz.model.vo.SavePanelUserVo;
import com.ksptool.ql.biz.service.panel.PanelUserService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.ksptool.entities.Entities.as;

/**
 * 用户管理控制器
 */
@Controller
@RequestMapping("/panel/user")
public class PanelUserController {

    @Autowired
    private PanelUserService panelUserService;

    /**
     * 用户管理页面
     */
    @GetMapping("/list")
    @RequirePermission("panel:user:view")
    public ModelAndView getListView(ListPanelUserDto dto) {
        ModelAndView mav = new ModelAndView("panel-user-manager");
        mav.addObject("data", panelUserService.getListView(dto));
        return mav;
    }

    /**
     * 用户操作页面（创建/编辑）
     */
    @GetMapping({"/create", "/edit"})
    @RequirePermission("panel:user:edit")
    public ModelAndView userOperator(@RequestParam(name = "id", required = false) Long id, @ModelAttribute("data") SavePanelUserVo flashData) {

        ModelAndView mav = new ModelAndView("panel-user-operator");
        
        try {
            SavePanelUserVo data;
            if (flashData != null && flashData.getUsername() != null) {
                // 使用flash中的数据，但需要重新获取用户组列表
                data = panelUserService.getCreateView();
                data = Any.of(flashData).val("groups",data.getGroups()).as(SavePanelUserVo.class);
            } else if (id != null) {
                // 编辑模式
                data = panelUserService.getEditView(id);
            } else {
                // 创建模式
                data = panelUserService.getCreateView();
            }
            mav.addObject("data", data);
        } catch (BizException e) {
            // 用户不存在时返回列表页
            return new ModelAndView("redirect:/panel/user/list");
        }
        return mav;
    }

    /**
     * 保存用户
     */
    @PostMapping("/save")
    @RequirePermission("panel:user:edit")
    public ModelAndView saveUser(@Valid SavePanelUserDto dto, BindingResult bindingResult, RedirectAttributes ra) {
        ModelAndView mav = new ModelAndView();

        try {
            // 处理验证错误
            if (bindingResult.hasErrors()) {
                mav.setViewName("redirect:/panel/user/create");
                ra.addFlashAttribute("data", as(dto, SavePanelUserVo.class));
                ra.addFlashAttribute("vo", Result.error(bindingResult.getFieldError().getDefaultMessage()));
                return mav;
            }
            
            // 验证新建用户时密码必填
            if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().trim().isEmpty())) {
                mav.setViewName("redirect:/panel/user/create");
                ra.addFlashAttribute("data", as(dto, SavePanelUserVo.class));
                ra.addFlashAttribute("vo", Result.error("新建用户时密码不能为空"));
                return mav;
            }
            
            // 保存用户
            panelUserService.saveUser(dto);
            
            if (dto.getId() == null) {
                // 创建成功：显示成功消息，清空表单，返回创建页面继续创建
                mav.setViewName("redirect:/panel/user/create");
                ra.addFlashAttribute("vo", Result.success("已创建用户:" + dto.getUsername(), null));
            } else {
                // 编辑成功：显示成功消息，返回列表页
                mav.setViewName("redirect:/panel/user/list");
                ra.addFlashAttribute("vo", Result.success("已更新用户:" + dto.getUsername(), null));
            }
        } catch (BizException e) {
            // 保存失败，返回表单页面并显示错误信息
            mav.setViewName("redirect:/panel/user/create");
            ra.addFlashAttribute("data", as(dto, SavePanelUserVo.class));
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mav;
    }

    /**
     * 删除用户
     */
    @PostMapping("/remove/{id}")
    @RequirePermission("panel:user:delete")
    public ModelAndView deleteUser(@PathVariable(name = "id") Long id) {
        ModelAndView mav = new ModelAndView("panel-user-manager");
        
        try {
            panelUserService.deleteUser(id);
            mav.addObject("vo", Result.success("删除成功", null));
        } catch (Exception e) {
            mav.addObject("vo", Result.error("删除失败：" + e.getMessage()));
        }
        
        // 重新加载用户列表
        ListPanelUserDto dto = new ListPanelUserDto();
        dto.setPage(1);
        dto.setSize(10);
        mav.addObject("data", panelUserService.getListView(dto));
        
        return mav;
    }
}