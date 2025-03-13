package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.ConfigService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Router {

    @Autowired
    private AuthService authService;

    @Autowired
    private ConfigService configService;

    @GetMapping("/")
    public String index(HttpServletRequest hsr) {
        //当前登录用户无效则进入登录页
        if(authService.verifyUser(hsr) == null){
            return "redirect:/login";
        }

        //跳转到"应用中心"
        return "redirect:/appCenter";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest hsr) {
        //已登录则进入应用中心
        if(authService.verifyUser(hsr) != null){
            return "redirect:/appCenter";
        }

        return "login";
    }

    @GetMapping("/appCenter")
    public String appCenter() {
        return "forward:/ssr/appCenter";
    }

    @RequirePermission("task:mgr:view")
    @GetMapping("/taskManager")
    public ModelAndView taskManager() {
        return new ModelAndView("task-manager");
    }

    @GetMapping("/ssr/demo")
    public String demo() {
        return "demo";
    }

    @RequirePermission("panel:access")
    @GetMapping("/dashboard")
    public ModelAndView dashboard(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("demo-control-panel");
        mav.addObject("title", "管理台");
        
        // 获取来源页面的URL并保存到当前用户的配置中
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/dashboard")) {
            try {
                java.net.URL url = new java.net.URL(referer);
                String path = url.getPath();
                if (path.isEmpty()) {
                    path = "/";
                }
                configService.setValue("path.referer.dashboard", path, AuthService.getCurrentUserId());
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                configService.setValue("path.referer.dashboard", "/ssr/appCenter", AuthService.getCurrentUserId());
            }
        }
        
        return mav;
    }

    @GetMapping("/leaveDashboard")
    public String leaveDashboard() {
        String referer = configService.get("path.referer.dashboard", "/ssr/appCenter");
        return "redirect:" + referer;
    }
    
    /**
     * 权限不足页面
     * 记录用户从哪个页面跳转过来的，存储在用户空间配置项
     */
    @GetMapping("/noPermission")
    public ModelAndView noPermission(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/no-permission");
        
        // 获取来源页面的URL并保存到当前用户的配置中
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/noPermission")) {
            try {
                java.net.URL url = new java.net.URL(referer);
                String path = url.getPath();
                if (path.isEmpty()) {
                    path = "/";
                }
                configService.setValue("path.referer.nopermission", path, AuthService.getCurrentUserId());
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                configService.setValue("path.referer.nopermission", "/", AuthService.getCurrentUserId());
            }
        }
        
        return mav;
    }
    
    /**
     * 离开权限不足页面
     * 从配置项中获取之前的页面并重定向过去
     */
    @GetMapping("/leaveNoPermission")
    public String leaveNoPermission() {
        String referer = configService.get("path.referer.nopermission", "/");
        return "redirect:" + referer;
    }

    @RequestMapping("/version")
    @ResponseBody
    public Result<String> getVersion(){
        return Result.success("1.1L-M7");
    }



}
