package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatSegmentRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ChatThreadRepository threadRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatSegmentRepository segmentRepository;

    public ChatThreadPo getPlayerThread(Long threadId,Integer type){

        return null;
    }


}
