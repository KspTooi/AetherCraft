package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.AbortConversationDto;
import com.ksptool.ql.biz.model.dto.QueryStreamDto;
import com.ksptool.ql.biz.model.dto.RegenerateDto;
import com.ksptool.ql.biz.model.dto.SendMessageDto;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.ChatConversationService;
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
@RequestMapping("/conversation")
public class ChatConversationController {

    @Autowired
    private ChatConversationService service;

    @PostMapping("/sendMessage")
    public Result<SendMessageVo> sendMessage(@RequestBody @Valid SendMessageDto dto) throws BizException {
        return Result.success(service.sendMessage(dto));
    }

    @PostMapping("/queryStream")
    public Result<MessageFragmentVo> queryStream(@RequestBody @Valid QueryStreamDto dto) throws BizException {
        return Result.success(service.queryMessage(dto));
    }

    @PostMapping("/regenerate")
    public Result<SendMessageVo> regenerate(@RequestBody @Valid RegenerateDto dto) throws BizException {
        return Result.success(service.regenerate(dto));
    }

    @PostMapping("/abortConversation")
    public Result<String> abortConversation(@RequestBody @Valid AbortConversationDto dto) {
        return null;
    }


}
