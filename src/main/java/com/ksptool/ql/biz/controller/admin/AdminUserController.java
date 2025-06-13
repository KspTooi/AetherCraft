package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetUserListDto;
import com.ksptool.ql.biz.model.dto.SaveUserDto;
import com.ksptool.ql.biz.model.vo.GetUserDetailsVo;
import com.ksptool.ql.biz.model.vo.GetUserListVo;
import com.ksptool.ql.biz.service.admin.AdminUserService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService service;


    @PostMapping("getUserList")
    @RequirePermissionRest("admin:user:view")
    public Result<RestPageableView<GetUserListVo>> getUserList(@RequestBody @Valid GetUserListDto dto){
        return Result.success(service.getUserList(dto));
    }

    @PostMapping("getUserDetails")
    @RequirePermissionRest("admin:user:save")
    public Result<GetUserDetailsVo> getUserDetails(@RequestBody @Valid CommonIdDto dto) {
        try{
            return Result.success(service.getUserDetails(dto.getId()));
        } catch (Exception ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PrintLog(sensitiveFields = "password")
    @PostMapping("saveUser")
    @RequirePermissionRest("admin:user:save")
    public Result<String> saveUser(@RequestBody @Valid SaveUserDto dto){
        try{
            service.saveUser(dto);
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeUser")
    @RequirePermissionRest("admin:user:delete")
    public Result<String> removeUser(@RequestBody @Valid CommonIdDto dto){
        try{
            service.removeUser(dto.getId());
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

}
