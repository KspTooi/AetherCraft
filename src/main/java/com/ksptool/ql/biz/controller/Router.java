package com.ksptool.ql.biz.controller;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.biz.service.panel.PanelInstallWizardService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private PlayerConfigService playerConfigService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private PanelInstallWizardService installWizardService;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${vite-dev-server.host}")
    private String viteDevServerHost;

    @Value("${vite-dev-server.port}")
    private String viteDevServerPort;

    @GetMapping("/")
    public String index(HttpServletRequest hsr) {
        // 如果启用向导模式，跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            return "redirect:/install-wizard/";
        }

        // 当前登录用户有效，跳转客户端UI
        if(authService.verifyUser(hsr) != null){
            return "redirect:/client-ui";
        }
        
        // 未登录用户跳转到欢迎页
        return "redirect:/welcome";
    }

    public String getViteDevServerURL(HttpServletRequest hsr){
        var scheme = hsr.getScheme();
        var serverName = hsr.getServerName();
        if(!viteDevServerHost.equals("auto")){
            serverName = viteDevServerHost;
        }
        var serverPort = viteDevServerPort;
        return scheme + "://" + serverName + ":" + serverPort;
    }

    @GetMapping("/client-ui")
    public ModelAndView clientUI(HttpServletRequest hsr) {

        var mav = new ModelAndView("client-ui-entry");

        // 如果启用向导模式，跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            mav.setViewName("redirect:/install-wizard/");
            return mav;
        }

        // 当前登录用户有效，跳转到客户端SPA页面
        if(authService.verifyUser(hsr) != null){

            //如果是开发模式 需要动态注入代理主机(VITE开发服务器)
            if(profile.equals("dev")){
                String host = getViteDevServerURL(hsr);
                mav.addObject("host", host);
            }

            return mav;
        }

        // 未登录用户跳转到欢迎页
        mav.setViewName("redirect:/welcome");
        return mav;
    }

    @GetMapping("/admin-ui")
    public ModelAndView adminUI(HttpServletRequest hsr) {

        var mav = new ModelAndView("admin-ui-entry");

        // 如果启用向导模式，跳转到向导
        if(installWizardService.hasInstallWizardMode()){
            mav.setViewName("redirect:/install-wizard/");
            return mav;
        }

        // 当前登录用户有效，跳转到客户端SPA页面
        if(authService.verifyUser(hsr) != null){

            //如果是开发模式 需要动态注入代理主机(VITE开发服务器)
            if(profile.equals("dev")){
                String host = getViteDevServerURL(hsr);
                mav.addObject("host", host);
            }

            return mav;
        }

        // 未登录用户跳转到欢迎页
        mav.setViewName("redirect:/welcome");
        return mav;
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

        //用户已登录则不再响应视图
        if (authService.verifyUser(hsr) != null) {
            return new ModelAndView("redirect:/client-ui");
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
            return new ModelAndView("redirect:/client-ui");
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

    @RequirePermission("panel:access")
    @GetMapping("/dashboard")
    public ModelAndView dashboard(HttpServletRequest request) {
        // 获取重定向参数
        String redirectView = request.getParameter("redirect");
        
        ModelAndView mav = new ModelAndView("redirect:/panel/model/role/list");
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
                playerConfigService.put("path.referer.dashboard", path);
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                playerConfigService.put("path.referer.dashboard", "/client-ui");
            }
        }
        
        return mav;
    }

    @GetMapping("/leaveDashboard")
    public String leaveDashboard() {
        String referer = playerConfigService.getString("path.referer.dashboard", "/client-ui");
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
                playerConfigService.put("path.referer.nopermission", path);
            } catch (Exception e) {
                // 如果URL解析失败，使用默认路径
                playerConfigService.put("path.referer.nopermission", "/");
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
        String referer = playerConfigService.getString("path.referer.nopermission", "/");
        return "redirect:" + referer;
    }

    @RequestMapping("/version")
    @ResponseBody
    public Result<String> getVersion(){
        return Result.success(AetherLauncher.getVersion());
    }



}
