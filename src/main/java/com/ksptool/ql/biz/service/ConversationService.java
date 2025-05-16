package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.dto.AbortConversationDto;
import com.ksptool.ql.biz.model.dto.QueryMessageDto;
import com.ksptool.ql.biz.model.dto.RegenerateDto;
import com.ksptool.ql.biz.model.dto.SendMessageDto;
import com.ksptool.ql.biz.model.vo.MessageFragmentVo;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class ConversationService {


    public MessageFragmentVo sendMessage(SendMessageDto dto) {

        




        return null;
    }


    public String regenerate(RegenerateDto dto) {
        return null;
    }


    public MessageFragmentVo queryMessage(QueryMessageDto dto) {
        return null;
    }


    public String abortConversation(AbortConversationDto dto) {
        return null;
    }


}
