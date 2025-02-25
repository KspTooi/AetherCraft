package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.ServiceQueryDto;
import com.ksptool.ql.biz.model.vo.WindowsServiceVo;
import com.ksptool.ql.biz.service.WindowsNativeService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/ssr")
@RequiredArgsConstructor
public class NativeServiceController {

    private final WindowsNativeService windowsNativeService;

    @RequirePermission("service:view")
    @GetMapping("/serviceCenter")
    public ModelAndView serviceCenter(@RequestParam(name = "keyword", required = false) String keyword) {
        ModelAndView mav = new ModelAndView("service-center");
        
        try {
            // 创建查询条件
            ServiceQueryDto queryDto = new ServiceQueryDto();
            queryDto.setServiceName(keyword);
            
            // 获取服务列表
            List<WindowsServiceVo> services = windowsNativeService.getNativeServiceList(queryDto);
            
            // 添加到模型
            mav.addObject("services", services);
            
        } catch (BizException e) {
            // 发生异常时添加错误信息
            mav.addObject("error", "获取服务列表失败：" + e.getMessage());
        }
        
        return mav;
    }
} 