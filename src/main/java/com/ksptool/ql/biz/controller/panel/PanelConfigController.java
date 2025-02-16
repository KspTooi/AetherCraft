package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListPanelConfigDto;
import com.ksptool.ql.biz.model.dto.SavePanelConfigDto;
import com.ksptool.ql.biz.model.vo.SavePanelConfigVo;
import com.ksptool.ql.biz.service.panel.PanelConfigService;
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

@Controller
@RequestMapping("/panel/config")
public class PanelConfigController {

    @Autowired
    private PanelConfigService panelConfigService;

    /**
     * 配置项列表页面
     */
    @GetMapping("/list")
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
    public ModelAndView getCreateView(@ModelAttribute("data") SavePanelConfigVo flashData) {
        ModelAndView mv = new ModelAndView("panel-config-operator");
        if (flashData != null && flashData.getId() != null) {
            mv.addObject("data", flashData);
            return mv;
        }
        
        mv.addObject("data", panelConfigService.getCreateView());
        return mv;
    }

    /**
     * 编辑配置项页面
     */
    @GetMapping("/edit")
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
    public String save(@Valid SavePanelConfigDto dto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // 验证失败，返回表单页面
        if (bindingResult.hasErrors()) {
            SavePanelConfigVo vo = new SavePanelConfigVo();
            assign(dto, vo);
            redirectAttributes.addFlashAttribute("data", vo);
            redirectAttributes.addFlashAttribute("vo", Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
            if (dto.getId() == null) {
                return "redirect:/panel/config/create";
            }
            return "redirect:/panel/config/edit?id=" + dto.getId();
        }

        try {
            // 保存配置项
            panelConfigService.save(dto);
            redirectAttributes.addFlashAttribute("vo", Result.success("保存成功", null));
            return "redirect:/panel/config/list";
        } catch (BizException e) {
            // 业务异常，返回表单页面并显示错误信息
            SavePanelConfigVo vo = new SavePanelConfigVo();
            assign(dto, vo);
            redirectAttributes.addFlashAttribute("data", vo);
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
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