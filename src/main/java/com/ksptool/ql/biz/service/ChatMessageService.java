package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatMessageRepository;
import com.ksptool.ql.biz.mapper.ChatSegmentRepository;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetThreadListDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;

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


    //获取对话历史列表
    public RestPageableView<GetThreadListVo> getThreadList(GetThreadListDto dto){

        var query = new ChatThreadPo();
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        query.setNpc(Any.of().val("id",dto.getNpcId()).as(NpcPo.class));
        query.setType(dto.getType());

        Page<ChatThreadPo> pPos = threadRepository.getThreadListWithLastMessage(query, dto.pageRequest());
        List<GetThreadListVo> vos = new ArrayList<>();

        for(var po : pPos.getContent()){
            GetThreadListVo vo = as(po,GetThreadListVo.class);

            // 获取最后一条消息（如果有）作为预览
            vo.setLastMessage("无消息……");

            if(po.getLastMessage() != null){
                //截取前50个字符作为预览
                String content = css.decryptForCurUser(po.getLastMessage().getContent());
                vo.setLastMessage(content.length() > 50 ?
                        content.substring(0, 50) + "..." :
                        content);
            }

            vos.add(vo);
        }

        return new RestPageableView<>(vos, pPos.getTotalElements());
    }

    @Transactional
    public void removeThread(Long threadId)throws BizException{

        //查询当前玩家下的Thread
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ChatThreadPo po = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("消息记录不存在或无权访问"));

        //移除关联记录
        segmentRepository.removeByThreadId(po.getId());
        messageRepository.removeByThreadId(po.getId());

        //移除Thread
        threadRepository.delete(po);
    }



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
        segmentRepository.removeByMessageId(messageId);
        messageRepository.delete(messagePo);
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

    public ChatThreadPo getThread(long threadId,long playerId){
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
        return threadRepository.findOne(Example.of(query)).orElse(null);
    }

    //创建新的标准会话
    public ChatThreadPo createThread(long userId,long playerId,String modelCode){
        var user = Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class);
        var player = Any.of().val("id",playerId).as(PlayerPo.class);
        var insert = new ChatThreadPo();
        insert.setType(0); //0:标准会话 1:RP会话 2:标准增强会话
        insert.setUser(user);
        insert.setPlayer(player);
        insert.setNpc(null);
        insert.setTitle("新会话");
        insert.setPublicInfo(null);
        insert.setDescription(null);
        insert.setTitleGenerated(0);
        insert.setModelCode(modelCode);
        insert.setActive(1);
        insert.setMessages(new ArrayList<>());
        return threadRepository.save(insert);
    }





}
