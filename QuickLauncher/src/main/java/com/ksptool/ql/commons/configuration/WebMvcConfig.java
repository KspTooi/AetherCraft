package com.ksptool.ql.commons.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/views/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/views/js/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/views/img/");
        registry.addResourceHandler("/commons/**").addResourceLocations("classpath:/views/commons/");
        registry.addResourceHandler("/res/**").addResourceLocations("file:userdata/res/");
    }
}