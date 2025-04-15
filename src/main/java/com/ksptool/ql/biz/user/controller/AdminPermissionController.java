package com.ksptool.ql.biz.user.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.user.model.dto.GetPermissionListDto;
import com.ksptool.ql.biz.user.model.dto.GetPermissionDetailsDto;
import com.ksptool.ql.biz.user.model.vo.GetPermissionDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetPermissionListVo;
import com.ksptool.ql.biz.user.service.AdminPermissionService;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/permission")
public class AdminPermissionController {

    @Autowired
    private AdminPermissionService service;

    @PostMapping("getPermissionList")
    public Result<RestPageableView<GetPermissionListVo>> getPermissionList(@RequestBody @Valid GetPermissionListDto dto){
        return Result.success(service.getPermissionList(dto));
    }

    @PostMapping("getPermissionDetails")
    public Result<GetPermissionDetailsVo> getPermissionDetails(@RequestBody @Valid CommonIdDto dto){
        try{
            return Result.success(service.getPermissionDetails(dto.getId()));
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("savePermission")
    public Result<String> savePermission(@RequestBody @Valid GetPermissionDetailsDto dto){
        try{
            service.savePermission(dto);
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removePermission")
    public Result<String> removePermission(@RequestBody @Valid CommonIdDto dto){
        try{
            service.removePermission(dto.getId());
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }
}
