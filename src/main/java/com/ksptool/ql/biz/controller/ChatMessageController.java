package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.EditMessageDto;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.service.ChatMessageService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService service;


    //获取对话历史列表
    @PostMapping("/getThreadList")
    public Result<RestPageableView<GetThreadListVo>> getThreadList(@RequestBody @Valid GetThreadListDto dto){

        //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        if(dto.getType() == 1 && dto.getNpcId() == null){
            return Result.error("NpcId不可为空");
        }

        return Result.success(service.getThreadList(dto));
    }


    //编辑对话消息
    @PostMapping("/editMessage")
    public Result<String> editMessage(@RequestBody @Valid EditMessageDto dto){
        try {
            service.editMessage(dto.getMessageId(),dto.getContent());
            return Result.success("操作成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }



}
