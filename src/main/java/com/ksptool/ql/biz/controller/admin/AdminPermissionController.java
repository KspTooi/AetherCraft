package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetPermissionListDto;
import com.ksptool.ql.biz.model.dto.SavePermissionDto;
import com.ksptool.ql.biz.model.vo.GetPermissionDefinitionVo;
import com.ksptool.ql.biz.model.vo.GetPermissionDetailsVo;
import com.ksptool.ql.biz.model.vo.GetPermissionListVo;
import com.ksptool.ql.biz.service.admin.AdminPermissionService;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/permission")
public class AdminPermissionController {

    @Autowired
    private AdminPermissionService service;


    @PostMapping("getPermissionDefinition")
    public Result<List<GetPermissionDefinitionVo>> getPermissionDefinition(){
        return Result.success(service.getPermissionDefinition());
    }

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
    public Result<String> savePermission(@RequestBody @Valid SavePermissionDto dto){
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
