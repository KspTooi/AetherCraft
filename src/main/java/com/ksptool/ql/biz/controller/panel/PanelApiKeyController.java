package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.model.dto.ListApiKeyAuthDto;
import com.ksptool.ql.biz.model.dto.SaveApiKeyDto;
import com.ksptool.ql.biz.model.vo.SaveApiKeyVo;
import com.ksptool.ql.biz.model.dto.SaveApiKeyAuthDto;
import com.ksptool.ql.biz.model.vo.SaveApiKeyAuthVo;
import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

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
        SaveApiKeyVo vo;
        
        // 如果有表单验证错误或业务异常，使用flashData
        if (flash != null && flash.getKeyName() != null) {
            vo = as(flash, SaveApiKeyVo.class);
        } else {
            // 第一次进入，初始化默认创建字段
            vo = new SaveApiKeyVo();
        }
        
        // 设置系列列表到VO
        vo.setKeySeriesList(AIModelEnum.getSeriesList());
        mv.addObject("data", vo);
        mv.addObject("title", "创建API密钥");
        return mv;
    }

    /**
     * 编辑API密钥页面
     */
    @GetMapping("/edit")
    public ModelAndView getEditView(@RequestParam("id") Long id, RedirectAttributes ra) {
        ModelAndView mav = new ModelAndView("panel-api-key-operator");

        try {
            // 获取编辑视图VO
            mav.addObject("data", panelApiKeyService.getEditView(id));
            mav.addObject("title", "编辑API密钥");
        } catch (BizException e) {
            // API密钥不存在时返回列表页
            mav.setViewName("redirect:/panel/model/apikey/list");
            ra.addFlashAttribute("vo", Result.error(e));
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


    /**
     * API密钥授权列表页面
     */
    @GetMapping("/auth/list")
    public ModelAndView getAuthListView(ListApiKeyAuthDto dto,RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView("panel-api-key-auth");
        
        try {
            // 获取并设置数据
            mv.addObject("data", panelApiKeyService.getAuthListView(dto));
        } catch (BizException e) {
            // API密钥不存在或无权访问时返回列表页
            mv.setViewName("redirect:/panel/model/apikey/list");
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mv;
    }

    /**
     * 创建API密钥授权页面
     */
    @GetMapping("/auth/create")
    public ModelAndView getAuthCreateView(@RequestParam("apiKeyId") Long apiKeyId, 
                                        @ModelAttribute("data") SaveApiKeyAuthDto flash) {
        ModelAndView mv = new ModelAndView("panel-api-key-auth-operator");
        
        try {
            // 如果有表单验证错误或业务异常，使用flashData
            if (flash != null && flash.getApiKeyId() != null) {
                mv.addObject("data", as(flash, SaveApiKeyAuthVo.class));
                return mv;
            }
            
            // 获取并设置数据
            mv.addObject("data", panelApiKeyService.getAuthCreateView(apiKeyId));
            mv.addObject("title", "创建API密钥授权");
        } catch (BizException e) {
            // API密钥不存在或无权访问时返回列表页
            mv.setViewName("redirect:/panel/model/apikey/list");
            mv.addObject("vo", Result.error(e.getMessage()));
        }
        
        return mv;
    }

    /**
     * 编辑API密钥授权页面
     */
    @GetMapping("/auth/edit")
    public ModelAndView getAuthEditView(@RequestParam("id") Long id,RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView("panel-api-key-auth-operator");
        
        try {
            // 获取并设置数据
            mv.addObject("data", panelApiKeyService.getAuthEditView(id));
            mv.addObject("title", "编辑API密钥授权");
        } catch (BizException e) {
            // 授权不存在或无权访问时返回列表页
            mv.setViewName("redirect:/panel/model/apikey/list");
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mv;
    }

    /**
     * 保存API密钥授权
     */
    @PostMapping("/auth/save")
    public ModelAndView saveAuth(@Valid SaveApiKeyAuthDto dto, BindingResult br, RedirectAttributes ra) {
        var createMode = dto.getId() == null;
        var editMode = dto.getId() != null;
        
        // 默认为创建模式，完成后返回创建页面继续创建
        ModelAndView mv = new ModelAndView("redirect:/panel/model/apikey/auth/create?apiKeyId=" + dto.getApiKeyId());
        
        // 编辑模式需重定向到编辑视图
        if (editMode) {
            mv.setViewName("redirect:/panel/model/apikey/auth/edit?id=" + dto.getId());
        }
        
        // 验证失败，返回表单页面
        if (br.hasErrors()) {
            ra.addFlashAttribute("data", dto);
            ra.addFlashAttribute("vo", Result.error(br));
            return mv;
        }
        
        try {
            // 保存授权
            panelApiKeyService.saveAuth(dto);
            
            if (createMode) {
                ra.addFlashAttribute("vo", Result.success("已创建授权", null));
            }
            
            if (editMode) {
                ra.addFlashAttribute("vo", Result.success("已更新授权", null));
            }
            
        } catch (BizException e) {
            // 业务异常
            ra.addFlashAttribute("vo", Result.error(e));
            
            // 创建模式需回显用户输入到视图
            if (createMode) {
                ra.addFlashAttribute("data", dto);
            }
        }
        
        return mv;
    }

    /**
     * 移除API密钥授权
     */
    @GetMapping("/auth/remove/{id}")
    public ModelAndView removeAuth(@PathVariable("id") Long id, 
                                 @RequestParam("apiKeyId") Long apiKeyId, 
                                 RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView("redirect:/panel/model/apikey/auth/list");
        mv.addObject("apiKeyId", apiKeyId);
        
        try {
            // 移除授权
            panelApiKeyService.removeAuth(id);
            ra.addFlashAttribute("vo", Result.success("已移除授权", null));
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e));
        }
        
        return mv;
    }

    /**
     * 移除API密钥
     */
    @GetMapping("/remove/{id}")
    public ModelAndView remove(@PathVariable("id") Long id, RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView("redirect:/panel/model/apikey/list");
        
        try {
            panelApiKeyService.removeApiKey(id);
            ra.addFlashAttribute("vo", Result.success("已移除API密钥", null));
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e));
        }
        
        return mv;
    }

} 