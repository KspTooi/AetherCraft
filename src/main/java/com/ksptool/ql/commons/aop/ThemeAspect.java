package com.ksptool.ql.commons.aop;

import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 主题配置切面
 */
@ControllerAdvice
public class ThemeAspect {

    @Autowired
    private UserConfigService userConfigService;

    @ModelAttribute("themeMainColor")
    public String injectMainColor() {
        String color = userConfigService.getValue(UserConfigEnum.CUSTOM_MAIN_COLOR.key());
        return StringUtils.defaultString(color, "#ffffff");
    }

    @ModelAttribute("themeMainFilter")
    public String injectMainFilter() {
        String filter = userConfigService.getValue(UserConfigEnum.CUSTOM_MAIN_FILTER.key());
        return StringUtils.defaultString(filter, "0");
    }

    @ModelAttribute("themeNavColor")
    public String injectNavColor() {
        String color = userConfigService.getValue(UserConfigEnum.CUSTOM_TOP_NAV_COLOR.key());
        return StringUtils.defaultString(color, "#ffffff");
    }

    @ModelAttribute("themeNavFilter")
    public String injectNavFilter() {
        String filter = userConfigService.getValue(UserConfigEnum.CUSTOM_TOP_NAV_FILTER.key());
        return StringUtils.defaultString(filter, "0");
    }

    @ModelAttribute("themeActiveStyle")
    public String injectActiveStyle() {
        String style = userConfigService.getValue(UserConfigEnum.CUSTOM_ACTIVE_STYLE.key());
        return StringUtils.defaultString(style, "default");
    }

    @ModelAttribute("themeButtonStyle")
    public String injectButtonStyle() {
        String style = userConfigService.getValue(UserConfigEnum.CUSTOM_BUTTON_STYLE.key());
        return StringUtils.defaultString(style, "default");
    }
} 