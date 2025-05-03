package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetGroupListDto;
import com.ksptool.ql.biz.model.dto.SaveGroupDto;
import com.ksptool.ql.biz.model.vo.GetGroupDefinitionsVo;
import com.ksptool.ql.biz.model.vo.GetGroupDetailsVo;
import com.ksptool.ql.biz.model.vo.GetGroupListVo;
import com.ksptool.ql.biz.service.admin.AdminGroupService;
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
@RequestMapping("/admin/group")
public class AdminGroupController {

    @Autowired
    private AdminGroupService service;

    @PostMapping("getGroupDefinitions")
    public Result<List<GetGroupDefinitionsVo>> getGroupDefinitions(){
        return Result.success(service.getGroupDefinitions());
    }

    @PostMapping("getGroupList")
    public Result<RestPageableView<GetGroupListVo>> getGroupList(@RequestBody @Valid GetGroupListDto dto){
        return Result.success(service.getGroupList(dto));
    }

    @PostMapping("getGroupDetails")
    public Result<GetGroupDetailsVo> getGroupDetails(@RequestBody @Valid CommonIdDto dto){
        try{
            return Result.success(service.getGroupDetails(dto.getId()));
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("saveGroup")
    public Result<String> saveGroup(@RequestBody @Valid SaveGroupDto dto){
        try{
            service.saveGroup(dto);
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("removeGroup")
    public Result<String> removeGroup(@RequestBody @Valid CommonIdDto dto){
        try{
            service.removeGroup(dto.getId());
            return Result.success("success");
        }catch (Exception ex){
            return Result.error(ex.getMessage());
        }
    }
}
