package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.vo.SaveApiKeyVo;
import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * API密钥管理控制器
 */
@Controller
@RequestMapping("/panel/model/apikey")
@RequiredArgsConstructor
public class PanelApiKeyController {
    
    private final PanelApiKeyService panelApiKeyService;

    /**
     * API密钥列表页面
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListApiKeyDto dto) {
        ModelAndView mv = new ModelAndView("panel-api-key");
        // 设置页面标题
        mv.addObject("title", "API密钥管理");
        // 获取并设置数据
        mv.addObject("data", panelApiKeyService.getListView(dto));
        return mv;
    }

    /**
     * 创建API密钥页面
     */
    @GetMapping("/create")
    public ModelAndView getCreateView(@ModelAttribute("data") SaveApiKeyDto flash) {
        ModelAndView mv = new ModelAndView("panel-api-key-operator");
        
        // 如果有表单验证错误或业务异常，使用flashData
        if (flash != null && flash.getKeyName() != null) {
            mv.addObject("data", as(flash,SaveApiKeyVo.class));
            return mv;
        }
        
        // 第一次进入，初始化默认创建字段
        mv.addObject("data", new SaveApiKeyVo());
        mv.addObject("title", "创建API密钥");
        return mv;
    }

    /**
     * 编辑API密钥页面
     */
    @GetMapping("/edit")
    public ModelAndView getEditView(@RequestParam("id") Long id) {

        ModelAndView mav = new ModelAndView("panel-api-key-operator");

        try {
            mav.addObject("data", panelApiKeyService.getEditView(id));
            mav.addObject("title", "编辑API密钥");
        } catch (BizException e) {
            // API密钥不存在时返回列表页
            mav.setViewName("redirect:/panel/model/apikey/list");
            mav.addObject("vo", Result.error(e.getMessage()));
        }

        return mav;
    }

    /**
     * 保存API密钥
     */
    @PostMapping("/save")
    public ModelAndView save(@Valid SaveApiKeyDto dto, BindingResult br, RedirectAttributes ra) {

        var createMode = dto.getId() == null;
        var editMode = dto.getId() != null;

        //默认为创建模式 完成后清空表单，返回创建页面继续创建
        ModelAndView mav = new ModelAndView("redirect:/panel/model/apikey/create");

        //编辑模式需重定向到编辑视图
        if(editMode){
            mav.setViewName("redirect:/panel/model/apikey/edit?id=" + dto.getId());
        }

        // 验证失败，返回表单页面
        if (br.hasErrors()) {
            ra.addFlashAttribute("data", as(dto,SaveApiKeyDto.class));
            ra.addFlashAttribute("vo", Result.error(br));
            return mav;
        }

        try {
            // 保存密钥
            panelApiKeyService.saveApiKey(dto);

            if (createMode) {
                ra.addFlashAttribute("vo", Result.success(String.format("已创建密钥:%s", dto.getKeyName()), null));
            }

            if(editMode){
                ra.addFlashAttribute("vo", Result.success(String.format("已编辑密钥:%s", dto.getKeyName()), null));
            }

        } catch (BizException e) {
            //业务异常
            ra.addFlashAttribute("vo", Result.error(e));

            //创建模式需回显用户输入到视图
            if(createMode){
                ra.addFlashAttribute("data", dto);
            }

        }

        return mav;
    }
} 