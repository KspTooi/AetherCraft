package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.AddPlayerDefaultGroupDto;
import com.ksptool.ql.biz.model.dto.RemovePlayerDefaultGroupDto;
import com.ksptool.ql.biz.model.vo.GetPlayerDefaultGroupListVo;
import com.ksptool.ql.biz.service.admin.AdminPlayerDefaultGroupService;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.web.PageQuery;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/player/default/group")
public class AdminPlayerDefaultGroupController {

    @Autowired
    private AdminPlayerDefaultGroupService service;

    @PostMapping("getPlayerDefaultGroupList")
    @RequirePermissionRest("admin:player:default:group:view")
    public Result<RestPageableView<GetPlayerDefaultGroupListVo>> getPlayerDefaultGroupList(@RequestBody @Valid PageQuery dto) {
        RestPageableView<GetPlayerDefaultGroupListVo> result = service.getPlayerDefaultGroupList(dto);
        return Result.success(result);
    }

    @PostMapping("/removePlayerDefaultGroup")
    @RequirePermissionRest("admin:player:default:group:remove")
    public Result<String> removePlayerDefaultGroup(@RequestBody @Valid RemovePlayerDefaultGroupDto dto) {
        service.removePlayerDefaultGroup(dto);
        return Result.success("操作成功");
    }

    @PostMapping("/addPlayerDefaultGroup")
    @RequirePermissionRest("admin:player:default:group:add")
    public Result<String> addPlayerDefaultGroup(@RequestBody @Valid AddPlayerDefaultGroupDto dto) {
        service.addPlayerDefaultGroup(dto);
        return Result.success("操作成功");
    }

}
