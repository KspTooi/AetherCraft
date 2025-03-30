package com.ksptool.ql.biz.controller;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.UserConfigService;
import com.ksptool.ql.biz.service.panel.PanelInstallWizardService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Router {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private PanelInstallWizardService installWizardService;

    @GetMapping("/")
    public String index(HttpServletRequest hsr) {
        // 如果启用向导模式，跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            return "redirect:/install-wizard/";
        }

        // 当前登录用户有效，跳转到应用中心(旧版)
        if(authService.verifyUser(hsr) != null){
            return "redirect:/model/chat/view";
        }
        
        // 未登录用户跳转到欢迎页
        return "redirect:/welcome";
    }

    @GetMapping("/client-ui")
    public String clientUI(HttpServletRequest hsr) {
        // 如果启用向导模式，跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            return "redirect:/install-wizard/";
        }

        // 当前登录用户有效，跳转到客户端SPA页面
        if(authService.verifyUser(hsr) != null){
            return "client-ui-entry";
        }

        // 未登录用户跳转到欢迎页
        return "redirect:/welcome";
    }



    @GetMapping("/welcome")
    public ModelAndView welcome() {
        // 获取是否允许用户注册的配置
        String allowRegister = globalConfigService.getValue(GlobalConfigEnum.ALLOW_USER_REGISTER.getKey());
        
        ModelAndView mav = new ModelAndView("welcome");
        mav.addObject("allowRegister", StringUtils.isNotBlank(allowRegister) && "true".equals(allowRegister));
        return mav;
    }

    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest hsr) {

        //如果启用向导模式 跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            return new ModelAndView("redirect:/install-wizard/");
        }

        if (authService.verifyUser(hsr) != null) {
            return new ModelAndView("redirect:/model/chat/view");
        }

        String loginBrand = globalConfigService.getValue(GlobalConfigEnum.PAGE_LOGIN_BRAND.getKey());
        String allowRegister = globalConfigService.getValue(GlobalConfigEnum.ALLOW_USER_REGISTER.getKey());
        
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("loginBrand", StringUtils.isBlank(loginBrand) ? "" : loginBrand);
        mav.addObject("allowRegister", StringUtils.isNotBlank(allowRegister) && "true".equals(allowRegister));
        return mav;
    }

    @GetMapping("/register")
    public ModelAndView register(HttpServletRequest hsr, RedirectAttributes ra) {

        if (authService.verifyUser(hsr) != null) {
            return new ModelAndView("redirect:/model/chat/view");
        }
        String allowRegister = globalConfigService.getValue(GlobalConfigEnum.ALLOW_USER_REGISTER.getKey());

        if(StringUtils.isBlank(allowRegister) || allowRegister.equals("false")){
            ra.addFlashAttribute("vo", Result.error("管理员已禁用注册!"));
            return new ModelAndView("redirect:/login");
        }

        String loginBrand = globalConfigService.getValue(GlobalConfigEnum.PAGE_LOGIN_BRAND.getKey());
        ModelAndView mav = new ModelAndView("register");
        mav.addObject("loginBrand", StringUtils.isBlank(loginBrand) ? "" : loginBrand);
        return mav;
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
        // 获取重定向参数
        String redirectView = request.getParameter("redirect");
        
        ModelAndView mav = new ModelAndView("demo-control-panel");
        mav.addObject("title", "管理台");

        // 如果指定了视图参数，则重定向到指定视图
        if (StringUtils.isNotBlank(redirectView)) {
            mav.setViewName("redirect:" + redirectView);
        } 
        
        // 获取来源页面的URL并保存到当前用户的配置中
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/dashboard")) {
            try {
                java.net.URL url = new java.net.URL(referer);
                String path = url.getPath();
                if (path.isEmpty()) {
                    path = "/";
                }
                userConfigService.setValue("path.referer.dashboard", path, AuthService.getCurrentUserId());
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                userConfigService.setValue("path.referer.dashboard", "/model/chat/view", AuthService.getCurrentUserId());
            }
        }
        
        return mav;
    }

    @GetMapping("/leaveDashboard")
    public String leaveDashboard() {
        String referer = userConfigService.get("path.referer.dashboard", "/model/chat/view");
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
                userConfigService.setValue("path.referer.nopermission", path, AuthService.getCurrentUserId());
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                userConfigService.setValue("path.referer.nopermission", "/", AuthService.getCurrentUserId());
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
        String referer = userConfigService.get("path.referer.nopermission", "/");
        return "redirect:" + referer;
    }

    @RequestMapping("/version")
    @ResponseBody
    public Result<String> getVersion(){
        return Result.success(AetherLauncher.getVersion());
    }



}
