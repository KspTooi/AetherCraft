package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListPanelConfigDto;
import com.ksptool.ql.biz.model.dto.SavePanelConfigDto;
import com.ksptool.ql.biz.model.vo.SavePanelConfigVo;
import com.ksptool.ql.biz.service.panel.PanelConfigService;
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

import static com.ksptool.entities.Entities.assign;

/**
 * 配置项管理控制器
 */
@Controller
@RequestMapping("/panel/config")
public class PanelConfigController {

    @Autowired
    private PanelConfigService panelConfigService;

    /**
     * 配置项列表页面
     */
    @GetMapping("/list")
    @RequirePermission("panel:config:view")
    public ModelAndView getListView(ListPanelConfigDto dto) {
        ModelAndView mv = new ModelAndView("panel-config-list");
        mv.addObject("data", panelConfigService.getListView(dto));
        mv.addObject("query", dto);
        return mv;
    }

    /**
     * 创建配置项页面
     */
    @GetMapping("/create")
    @RequirePermission("panel:config:add")
    public ModelAndView getCreateView(@ModelAttribute("data") SavePanelConfigVo flashData) {
        ModelAndView mv = new ModelAndView("panel-config-operator");
        
        // 如果有表单验证错误或业务异常，使用flashData
        if (flashData != null && flashData.getConfigKey() != null) {
            mv.addObject("data", flashData);
            return mv;
        }
        
        // 第一次进入，初始化新的配置项
        mv.addObject("data", panelConfigService.getCreateView());
        return mv;
    }

    /**
     * 编辑配置项页面
     */
    @GetMapping("/edit")
    @RequirePermission("panel:config:edit")
    public ModelAndView getEditView(@RequestParam("id") Long id) {
        try {
            ModelAndView mv = new ModelAndView("panel-config-operator");

            // 否则从数据库获取数据
            SavePanelConfigVo data = panelConfigService.getEditView(id);
            mv.addObject("data", data);
            return mv;
        } catch (BizException e) {
            // 配置项不存在时返回列表页
            ModelAndView mv = new ModelAndView("redirect:/panel/config/list");
            mv.addObject("vo", Result.error(e.getMessage()));
            return mv;
        }
    }

    /**
     * 保存配置项
     */
    @PostMapping("/save")
    @RequirePermission("panel:config:edit")
    public String save(@Valid SavePanelConfigDto dto, BindingResult bindingResult, RedirectAttributes ra) {
        // 验证失败，返回表单页面
        if (bindingResult.hasErrors()) {
            SavePanelConfigVo vo = new SavePanelConfigVo();
            assign(dto, vo);
            ra.addFlashAttribute("data", vo);
            ra.addFlashAttribute("vo", Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
            if (dto.getId() == null) {
                return "redirect:/panel/config/create";
            }
            return "redirect:/panel/config/edit?id=" + dto.getId();
        }

        try {
            // 保存配置项
            panelConfigService.save(dto);
            
            // 判断是创建还是编辑模式
            if (dto.getId() == null) {
                // 创建成功：显示成功消息，清空表单，返回创建页面继续创建
                ra.addFlashAttribute("vo", Result.success(String.format("已创建配置项:%s", dto.getConfigKey()), null));
                return "redirect:/panel/config/create";
            } else {
                // 编辑成功：显示成功消息，返回列表页
                ra.addFlashAttribute("vo", Result.success(String.format("已更新配置项:%s", dto.getConfigKey()), null));
                return "redirect:/panel/config/list";
            }
        } catch (BizException e) {
            // 业务异常，返回表单页面并显示错误信息
            SavePanelConfigVo vo = new SavePanelConfigVo();
            assign(dto, vo);
            ra.addFlashAttribute("data", vo);
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            if (dto.getId() == null) {
                return "redirect:/panel/config/create";
            }
            return "redirect:/panel/config/edit?id=" + dto.getId();
        }
    }

    /**
     * 删除配置项
     */
    @GetMapping("/remove/{id}")
    @RequirePermission("panel:config:remove")
    public String remove(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            panelConfigService.remove(id);
            redirectAttributes.addFlashAttribute("vo", Result.success("删除成功", null));
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        return "redirect:/panel/config/list";
    }
} 