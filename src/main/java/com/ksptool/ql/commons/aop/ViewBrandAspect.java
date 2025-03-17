package com.ksptool.ql.commons.aop;

import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewBrandAspect {

    @Autowired
    private GlobalConfigService globalConfigService;

    @ModelAttribute("topBarBrand")
    public String injectBrandName() {
        String brandName = globalConfigService.getValue(GlobalConfigEnum.PAGE_TOP_BAR_BRAND.getKey());
        if (StringUtils.isBlank(brandName)) {
            return "";
        }
        return brandName;
    }
    
    @ModelAttribute("panelBrand")
    public String injectPanelBrandName() {
        String brandName = globalConfigService.getValue(GlobalConfigEnum.PAGE_PANEL_BRAND.getKey());
        if (StringUtils.isBlank(brandName)) {
            return "管理台";
        }
        return brandName;
    }
}