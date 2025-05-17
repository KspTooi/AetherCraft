package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.EditThreadDto;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
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

    //获取对话Thread列表
    @PostMapping("/getThreadList")
    public Result<RestPageableView<GetThreadListVo>> getThreadList(@RequestBody @Valid GetThreadListDto dto) throws BizException {

        //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        if(dto.getType() == 1 && dto.getNpcId() == null){
            return Result.error("NpcId不可为空");
        }

        return Result.success(service.getThreadList(dto));
    }

    //编辑Thread
    public Result<String> editThread(@RequestBody @Valid EditThreadDto dto) throws BizException {
        return null;
    }

    //移除聊天Thread
    @PostMapping("/removeThread")
    public Result<String> removeThread(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeThread(dto.getId());
        return Result.success("操作成功");
    }


}
