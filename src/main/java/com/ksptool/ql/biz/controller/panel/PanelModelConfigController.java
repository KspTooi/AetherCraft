package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.SaveModelConfigDto;
import com.ksptool.ql.biz.service.panel.PanelModelConfigService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AI模型配置控制器
 */
@Controller
@RequestMapping("/panel/model")
public class PanelModelConfigController {
    
    @Autowired
    private PanelModelConfigService panelModelConfigService;
    
    /**
     * 编辑AI模型配置页面
     */
    @GetMapping({"/edit", "/edit/{model}"})
    @RequirePermission("panel:model:view")
    public ModelAndView editPage(@PathVariable(name = "model", required = false) String model) {
        ModelAndView mv = new ModelAndView("panel-model-config");
        // 设置页面标题
        mv.addObject("title", "AI模型配置");
        // 获取配置数据
        mv.addObject("data", panelModelConfigService.getEditView(model));
        return mv;
    }
    
    /**
     * 保存AI模型配置
     */
    @PostMapping("/save")
    @RequirePermission("panel:model:edit")
    public String saveConfig(@Valid SaveModelConfigDto dto, BindingResult br, RedirectAttributes ra) {
        // 表单验证
        if (br.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error(br.getAllErrors().get(0).getDefaultMessage()));
            return "redirect:/panel/model/edit/" + dto.getModel();
        }
        
        try {
            // 保存配置
            panelModelConfigService.saveConfig(dto);
            ra.addFlashAttribute("vo", Result.success(String.format("已保存模型[%s]的配置", dto.getModel()), null));
        } catch (Exception e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return "redirect:/panel/model/edit/" + dto.getModel();
    }
    
    /**
     * 测试AI模型配置连通性
     */
    @PostMapping("/test")
    @RequirePermission("panel:model:edit")
    public String testModelConfig(@RequestParam(name = "model", required = true) String model, RedirectAttributes ra) {
        try {
            // 测试模型配置
            String response = panelModelConfigService.testModelConfig(model);
            // 截取响应，避免太长
            if (response.length() > 100) {
                response = response.substring(0, 100) + "...";
            }
            ra.addFlashAttribute("vo", Result.success(String.format("模型[%s]连接测试成功，响应: %s", model, response), null));
        } catch (Exception e) {
            ra.addFlashAttribute("vo", Result.error("测试失败: " + e.getMessage()));
        }
        
        return "redirect:/panel/model/edit/" + model;
    }
} 