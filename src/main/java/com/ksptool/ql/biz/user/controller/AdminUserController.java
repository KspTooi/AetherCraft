package com.ksptool.ql.biz.user.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.user.model.dto.GetUserListDto;
import com.ksptool.ql.biz.user.model.dto.SaveUserDto;
import com.ksptool.ql.biz.user.model.vo.GetUserDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetUserListVo;
import com.ksptool.ql.biz.user.service.AdminUserService;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService service;


    @PostMapping("getUserList")
    public Result<RestPageableView<GetUserListVo>> getUserList(@RequestBody @Valid GetUserListDto dto){
        return Result.success(service.getUserList(dto));
    }

    @PostMapping("getUserDetails")
    public Result<GetUserDetailsVo> getUserDetails(@RequestBody @Valid CommonIdDto dto) {
        try{
            return Result.success(service.getUserDetails(dto.getId()));
        } catch (Exception ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("saveUser")
    public Result<String> saveUser(@RequestBody @Valid SaveUserDto dto){
        try{
            service.saveUser(dto);
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeUser")
    public Result<String> removeUser(@RequestBody @Valid CommonIdDto dto){
        try{
            service.removeUser(dto.getId());
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

}
