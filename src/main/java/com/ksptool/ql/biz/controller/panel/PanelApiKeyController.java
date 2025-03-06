package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListApiKeyDto;
import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
} 