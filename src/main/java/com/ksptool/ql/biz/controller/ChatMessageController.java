package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.EditMessageDto;
import com.ksptool.ql.biz.service.ChatMessageService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.exception.BizException;
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
@RequestMapping("/message")
@PrintLog
public class ChatMessageController {

    @Autowired
    private ChatMessageService service;


    //编辑对话消息
    @PrintLog(sensitiveFields = "content")
    @PostMapping("/editMessage")
    public Result<String> editMessage(@RequestBody @Valid EditMessageDto dto) throws BizException {
        service.editMessage(dto.getMessageId(),dto.getContent());
        return Result.success("操作成功");
    }

    //移除聊天消息
    @PostMapping("/removeMessage")
    public Result<String> removeMessage(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeMessage(dto.getId());
        return Result.success("操作成功");
    }



}
