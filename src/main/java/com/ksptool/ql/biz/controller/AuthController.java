package com.ksptool.ql.biz.controller;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.model.dto.LoginDto;
import com.ksptool.ql.biz.model.dto.RegisterDto;
import com.ksptool.ql.biz.model.vo.LoginVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.UserService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login")
    public String login(@Valid LoginDto dto, HttpServletResponse response, RedirectAttributes ra) {
        try {
            var token = authService.loginByPassword(dto.getUsername(), dto.getPassword());
            
            // 设置 cookie
            var cookie = new jakarta.servlet.http.Cookie("token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);  // 防止 XSS 攻击
            cookie.setMaxAge(7 * 24 * 60 * 60);  // 7天有效期
            response.addCookie(cookie);
            
            // 登录成功，重定向到应用中心
            return "redirect:/ssr/appCenter";
        } catch (BizException e) {
            // 登录失败，添加错误消息并重定向回登录页
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping(value = "/register")
    @ResponseBody
    public Result<String> login(@Valid @RequestBody RegisterDto dto) {

        try{
            var register = userService.register(dto.getUsername(), dto.getPassword());
            return Result.success("注册成功:"+register.getUsername());
        }catch (BizException e){
            return Result.error(e);
        }

    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取当前用户
            var user = authService.verifyUser(request);
            // 清除数据库中的 session
            authService.logout(user);
            // 清除 HTTP session
            request.getSession().invalidate();
            // 重定向到登录页
            return "redirect:/login";
        } catch (Exception e) {
            // 如果发生异常（比如用户已经注销），也重定向到登录页
            return "redirect:/login";
        }
    }
}
