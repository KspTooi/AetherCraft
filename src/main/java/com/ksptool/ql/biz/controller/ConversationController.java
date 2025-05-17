package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.AbortConversationDto;
import com.ksptool.ql.biz.model.dto.QueryMessageDto;
import com.ksptool.ql.biz.model.dto.RegenerateDto;
import com.ksptool.ql.biz.model.dto.SendMessageDto;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.biz.model.vo.SendMessageVo;
import com.ksptool.ql.biz.service.ConversationService;
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
public class ConversationController {

    @Autowired
    private ConversationService service;

    @PostMapping("/sendMessage")
    public Result<SendMessageVo> sendMessage(@RequestBody @Valid SendMessageDto dto) {
        return null;
    }

    @PostMapping("/regenerate")
    public Result<String> regenerate(@RequestBody @Valid RegenerateDto dto) {
        return null;
    }

    @PostMapping("/queryMessage")
    public Result<MessageFragmentVo> queryMessage(@RequestBody @Valid QueryMessageDto dto) {
        return null;
    }

    @PostMapping("/abortConversation")
    public Result<String> abortConversation(@RequestBody @Valid AbortConversationDto dto) {
        return null;
    }


}
