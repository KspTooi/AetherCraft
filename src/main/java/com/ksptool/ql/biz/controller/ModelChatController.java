package com.ksptool.ql.biz.controller;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.biz.model.dto.ChatCompleteDto;
import com.ksptool.ql.biz.model.vo.ChatCompleteVo;
import com.ksptool.ql.biz.service.ModelChatService;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/model")
public class ModelChatController {
    
    @Autowired
    private ModelChatService modelChatService;

    @PostMapping("/chat/complete")
    public Result<ChatCompleteVo> chatComplete(@Valid @RequestBody ChatCompleteDto dto) {
        try {
            return Result.success(modelChatService.chatComplete(dto));
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 