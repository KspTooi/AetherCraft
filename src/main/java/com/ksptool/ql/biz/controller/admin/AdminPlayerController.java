package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetAdminPlayerListDto;
import com.ksptool.ql.biz.model.dto.EditAdminPlayerDto;
import com.ksptool.ql.biz.model.vo.GetAdminPlayerDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminPlayerListVo;
import com.ksptool.ql.biz.service.admin.AdminPlayerService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/player")
public class AdminPlayerController {

    @Autowired
    private AdminPlayerService service;

    @PostMapping("getPlayerList")
    public Result<RestPageableView<GetAdminPlayerListVo>> getPlayerList(@RequestBody @Valid GetAdminPlayerListDto dto){
        return Result.success(service.getPlayerList(dto));
    }

    @PostMapping("getPlayerDetails")
    public Result<GetAdminPlayerDetailsVo> getPlayerDetails(@RequestBody @Valid CommonIdDto dto){
        try {
            return Result.success(service.getPlayerDetails(dto.getId()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("editPlayer")
    public Result<String> editPlayer(@RequestBody @Valid EditAdminPlayerDto dto){
        try {
            service.editPlayer(dto);
            return Result.success("success");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

}
