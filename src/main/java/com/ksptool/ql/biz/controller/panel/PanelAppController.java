package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListPanelAppDto;
import com.ksptool.ql.biz.model.dto.SavePanelAppDto;
import com.ksptool.ql.biz.model.vo.SavePanelAppVo;
import com.ksptool.ql.biz.service.panel.PanelAppService;
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
@RequestMapping("/panel/app")
public class PanelAppController {

    @Autowired
    private PanelAppService panelAppService;

    /**
     * 应用列表页面
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListPanelAppDto dto) {
        ModelAndView mv = new ModelAndView("panel-app-list");
        mv.addObject("data", panelAppService.getListView(dto));
        mv.addObject("query", dto);
        return mv;
    }

    /**
     * 创建应用页面
     */
    @GetMapping("/create")
    public ModelAndView getCreateView(@ModelAttribute("data") SavePanelAppVo flashData) {
        ModelAndView mv = new ModelAndView("panel-app-operator");
        
        // 如果有表单验证错误或业务异常，使用flashData
        if (flashData != null && flashData.getName() != null) {
            mv.addObject("data", flashData);
            return mv;
        }
        
        // 第一次进入，初始化新的应用
        mv.addObject("data", panelAppService.getCreateView());
        return mv;
    }

    /**
     * 编辑应用页面
     */
    @GetMapping("/edit")
    public ModelAndView getEditView(@RequestParam("id") Long id) {
        try {
            ModelAndView mv = new ModelAndView("panel-app-operator");

            // 从数据库获取数据
            SavePanelAppVo data = panelAppService.getEditView(id);
            mv.addObject("data", data);
            return mv;
        } catch (BizException e) {
            // 应用不存在时返回列表页
            ModelAndView mv = new ModelAndView("redirect:/panel/app/list");
            mv.addObject("vo", Result.error(e.getMessage()));
            return mv;
        }
    }

    /**
     * 保存应用
     */
    @PostMapping("/save")
    public String save(@Valid SavePanelAppDto dto, BindingResult bindingResult, RedirectAttributes ra) {
        // 验证失败，返回表单页面
        if (bindingResult.hasErrors()) {
            SavePanelAppVo vo = new SavePanelAppVo();
            assign(dto, vo);
            ra.addFlashAttribute("data", vo);
            ra.addFlashAttribute("vo", Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage()));
            if (dto.getId() == null) {
                return "redirect:/panel/app/create";
            }
            return "redirect:/panel/app/edit?id=" + dto.getId();
        }

        try {
            // 保存应用
            panelAppService.save(dto);
            
            // 判断是创建还是编辑模式
            if (dto.getId() == null) {
                // 创建成功：显示成功消息，清空表单，返回创建页面继续创建
                ra.addFlashAttribute("vo", Result.success(String.format("已创建应用:%s", dto.getName()), null));
                return "redirect:/panel/app/create";
            } else {
                // 编辑成功：显示成功消息，返回列表页
                ra.addFlashAttribute("vo", Result.success(String.format("已更新应用:%s", dto.getName()), null));
                return "redirect:/panel/app/list";
            }
        } catch (BizException e) {
            // 业务异常，返回表单页面并显示错误信息
            SavePanelAppVo vo = new SavePanelAppVo();
            assign(dto, vo);
            ra.addFlashAttribute("data", vo);
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            if (dto.getId() == null) {
                return "redirect:/panel/app/create";
            }
            return "redirect:/panel/app/edit?id=" + dto.getId();
        }
    }

    /**
     * 删除应用
     */
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            panelAppService.remove(id);
            redirectAttributes.addFlashAttribute("vo", Result.success("删除成功", null));
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        return "redirect:/panel/app/list";
    }
} 