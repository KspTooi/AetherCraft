package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ChatThreadRepository;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatThreadService {

    @Autowired
    private ChatThreadRepository repository;

    @Autowired
    private GlobalConfigService globalConfigService;

    public ChatThreadPo getSelfThread(long threadId) throws BizException{
        var query = new ChatThreadPo();
        query.setId(threadId);
        query.setUser(Any.of().val("id",AuthService.requireUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.requirePlayerId()).as(PlayerPo.class));
        return repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或无权访问"));
    }

    @Transactional(rollbackFor = BizException.class)
    public void editThreadTitle(long threadId, String newTitle) throws BizException {

        var threadPo = this.getSelfThread(threadId);

        // 限制标题长度
        if (newTitle.length() > 100) {
            newTitle = newTitle.substring(0, 97) + "...";
        }

        threadPo.setTitle(newTitle);
        threadPo.setTitleGenerated(1);
        repository.save(threadPo);
    }

    @Async
    @Transactional(rollbackFor = BizException.class)
    public void generateThreadTitleAsync(long threadId) throws BizException {

        // 检查是否需要生成标题
        boolean shouldGenerateTitle = globalConfigService.getBoolean(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_TITLE.getKey(), true);
        if (!shouldGenerateTitle) {
            return; // 配置为不生成标题，直接返回
        }




    }





}
