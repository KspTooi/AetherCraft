package com.ksptool.ql.biz.controller.admin;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetSessionListDto;
import com.ksptool.ql.biz.model.vo.GetSessionDetailsVo;
import com.ksptool.ql.biz.model.vo.GetSessionListVo;
import com.ksptool.ql.biz.service.admin.AdminSessionService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/session")
public class AdminSessionController {

    @Autowired
    private AdminSessionService service;

    @PostMapping("getSessionList")
    @RequirePermissionRest("admin:session:view")
    public Result<RestPageableView<GetSessionListVo>> getSessionList(@RequestBody @Valid GetSessionListDto dto) {
        return Result.success(service.getSessionList(dto));
    }

    @PostMapping("getSessionDetails")
    @RequirePermissionRest("admin:session:view")
    public Result<GetSessionDetailsVo> getSessionDetails(@RequestBody @Valid CommonIdDto dto) {
        try {
            return Result.success(service.getSessionDetails(dto.getId()));
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("closeSession")
    @RequirePermissionRest("admin:session:close")
    public Result<String> closeSession(@RequestBody @Valid CommonIdDto dto) {
        try {
            service.closeSession(dto.getId());
            return Result.success("关闭成功");
        } catch (BizException ex) {
            return Result.error(ex.getMessage());
        }
    }
}
