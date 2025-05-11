package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.SaveNpcChatExampleDto;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.model.vo.GetNpcChatExampleListVo;
import com.ksptool.ql.biz.service.NpcChatExampleService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/npc/chatExample")
public class NpcChatExampleController {

    @Autowired
    private NpcChatExampleService service;

    @PostMapping("/getNpcChatExampleList")
    public Result<List<GetNpcChatExampleListVo>> getModelRoleList(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getModelRoleList(dto.getId()));
    }

    @PostMapping("saveNpcChatExample")
    public Result<String> saveNpcChatExample(@RequestBody @Valid SaveNpcChatExampleDto dto) throws BizException {
        service.saveNpcChatExample(dto);
        return Result.success("success");
    }

    @PostMapping("removeNpcChatExample")
    public Result<String> removeNpcChatExample(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeNpcChatExample(dto.getId());
        return Result.success("success");
    }

}
