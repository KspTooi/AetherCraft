package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.EditThreadTitleDto;
import com.ksptool.ql.biz.model.dto.SelectThreadDto;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.vo.SelectThreadVo;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.service.ChatThreadService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/thread")
public class ChatThreadController {

    @Autowired
    private ChatThreadService service;


    @PostMapping("/selectThread")
    public Result<SelectThreadVo> selectThread(@RequestBody @Valid SelectThreadDto dto) throws BizException {
        return Result.success(service.selectThread(dto));
    }

    @PostMapping("/getThreadList")
    public Result<RestPageableView<GetThreadListVo>> getThreadList(@RequestBody @Valid GetThreadListDto dto) throws BizException {

        //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        if(dto.getType() == 1 && dto.getNpcId() == null){
            return Result.error("NpcId不可为空");
        }

        return Result.success(service.getThreadList(dto));
    }

    @PostMapping("/editThreadTitle")
    public Result<String> editThreadTitle(@RequestBody @Valid EditThreadTitleDto dto) throws BizException {
        service.editThreadTitle(dto.getThreadId(),dto.getTitle());
        return Result.success("操作成功");
    }


    @PostMapping("/removeThread")
    public Result<String> removeThread(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeThread(dto.getId());
        return Result.success("操作成功");
    }


}
