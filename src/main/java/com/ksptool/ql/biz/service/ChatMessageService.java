package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ChatMessageService {

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ChatThreadRepository threadRepository;

    @Autowired
    private ChatMessageRepository messageRepository;


    //编辑对话历史消息
    @Transactional
    public void editMessage(Long messageId, String content) throws BizException {

        ChatMessagePo messagePo = messageRepository.findById(messageId)
                .orElseThrow(() -> new BizException("消息记录不存在"));

        ensureHasPermission(messagePo);

        messagePo.setContent(css.encryptForCurUser(content));
        messageRepository.save(messagePo);
    }

    //删除消息
    @Transactional
    public void removeMessage(Long messageId) throws BizException {

        ChatMessagePo messagePo = messageRepository.findById(messageId)
                .orElseThrow(() -> new BizException("消息记录不存在"));

        ensureHasPermission(messagePo);

        ChatThreadPo threadPo = messagePo.getThread();
        threadPo.setLastMessage(null);
        messageRepository.delete(messagePo);

        Optional<ChatMessagePo> topMessageOpt = messageRepository.getTopSeqMessageByThreadId(threadPo.getId());
        topMessageOpt.ifPresent(threadPo::setLastMessage);
        threadRepository.save(threadPo);
    }


    private void ensureHasPermission(ChatMessagePo po) throws BizException{

        ChatThreadPo thread = po.getThread();
        UserPo user = thread.getUser();
        PlayerPo player = thread.getPlayer();

        if(user == null || player == null){
            throw new BizException("无法找到消息所有者主体");
        }

        long uid = user.getId();
        long pid = player.getId();
        Long cuid = AuthService.getCurrentUserId();
        Long cpid = AuthService.getCurrentPlayerId();

        if(cuid == null || cpid == null){
            throw new BizException("无法找到当前人物主体");
        }

        if(uid != cuid || pid !=cpid){
            throw new BizException("无权限访问消息");
        }

    }

    public ChatMessagePo getSelfMessage(long messageId) throws BizException{
        ChatMessagePo messagePo = messageRepository.findById(messageId)
                .orElseThrow(() -> new BizException("消息记录不存在"));
        ensureHasPermission(messagePo);
        return messagePo;
    }




}
