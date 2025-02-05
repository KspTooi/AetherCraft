package com.ksptool.ql.biz.controller;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.model.dto.LoginDto;
import com.ksptool.ql.biz.model.dto.RegisterDto;
import com.ksptool.ql.biz.model.vo.LoginVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.UserService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDto dto) {

        try{
            var token = authService.loginByPassword(dto.getUsername(), dto.getPassword());
            return Result.success(Any.of().val("token",token).as(LoginVo.class));
        }catch (BizException e){
            return Result.error(e);
        }

    }

    @PostMapping(value = "/register")
    public Result<String> login(@Valid @RequestBody RegisterDto dto) {

        try{
            var register = userService.register(dto.getUsername(), dto.getPassword());
            return Result.success("注册成功:"+register.getUsername());
        }catch (BizException e){
            return Result.error(e);
        }

    }
}
