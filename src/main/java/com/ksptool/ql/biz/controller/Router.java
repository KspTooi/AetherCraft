package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.WebUtils;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Router {

    @Autowired
    private AuthService authService;

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

    @GetMapping("/taskManager")
    public String taskManager() {
        return "task-manager";
    }

    @GetMapping("/ssr/demo")
    public String demo() {
        return "demo";
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        ModelAndView mav = new ModelAndView("demo-control-panel");
        mav.addObject("title", "管理台");
        return mav;
    }

    @RequestMapping("/version")
    @ResponseBody
    public Result<String> getVersion(){
        return Result.success("1.0-A");
    }
}
