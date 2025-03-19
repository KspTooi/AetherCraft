package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelRpThreadRepository;
import com.ksptool.ql.biz.mapper.ModelRpHistoryRepository;
import com.ksptool.ql.biz.model.po.ModelRpThreadPo;
import com.ksptool.ql.biz.model.po.ModelRpHistoryPo;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;


/**
 * 模型RP剧本与场景服务
 */
@Service
public class ModelRpScriptService {

    @Autowired
    private ModelRpThreadRepository threadRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ContentSecurityService contentSecurityService;

    @Autowired
    private ModelRpHistoryRepository historyRepository;

    /**
     * 创建新的RP对话存档
     * 会将同一角色下的其他存档设置为非激活状态，并创建一个新的激活状态的存档
     * 如果模型角色有首条消息，会同时创建首条AI消息
     *
     * @param userPlayRolePt 用户扮演的角色
     * @param modelPlayRolePt 模型扮演的角色
     * @throws BizException 如果创建过程中出现错误
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewThread(ModelUserRolePo userPlayRolePt, ModelRolePo modelPlayRolePt,String modeCode) throws BizException {

        //取消该用户该角色下所有存档的激活状态
        threadRepository.setAllThreadActive(AuthService.getCurrentUserId(), modelPlayRolePt.getId(), 0);
        entityManager.clear();

        //创建新存档
        ModelRpThreadPo thread = new ModelRpThreadPo();
        thread.setUserId(AuthService.getCurrentUserId());
        thread.setModelCode(modeCode);
        thread.setModelRole(modelPlayRolePt);
        thread.setUserRole(userPlayRolePt);
        thread.setTitle(userPlayRolePt.getName() + "与" + modelPlayRolePt.getName() + "的对话");
        thread.setActive(1);
        
        //加密存档内容
        contentSecurityService.process(thread, true);
        threadRepository.save(thread);

        //如果模型角色有首条消息，创建首条AI消息
        if (StringUtils.isNotBlank(modelPlayRolePt.getFirstMessage())) {

            //准备Prompt
            var prompt = PreparedPrompt.prepare(modelPlayRolePt.getFirstMessage());
            prompt.setParameter("model",modelPlayRolePt.getName());
            prompt.setParameter("user",userPlayRolePt.getName());

            ModelRpHistoryPo history = new ModelRpHistoryPo();
            history.setThread(thread);
            history.setType(1); // AI消息
            history.setRawContent(prompt.execute());
            history.setRpContent(modelPlayRolePt.getFirstMessage()); // 这里可能需要通过RpHandler处理
            history.setSequence(1);
            contentSecurityService.process(history,true);
            historyRepository.save(history);
        }

        entityManager.clear();
    }

    /**
     * 激活指定的RP对话存档
     * 同时会将该用户同一角色下的其他存档设置为非激活状态
     *
     * @param threadId 要激活的存档ID
     * @throws BizException 如果存档不存在或不属于当前用户
     */
    @Transactional
    public void activeThread(Long threadId) throws BizException {
        Long currentUserId = AuthService.getCurrentUserId();
        
        //查询指定ID的存档
        var query = new ModelRpThreadPo();
        query.setUserId(currentUserId);
        query.setId(threadId);

        ModelRpThreadPo thread = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("ThreadId无效!"));
                
        //获取该存档的角色ID
        Long roleId = thread.getModelRole().getId();

        //取消全部Thread的激活状态
        threadRepository.setAllThreadActive(currentUserId, roleId, 0);
        entityManager.clear();

        //激活指定存档
        thread.setActive(1);
        threadRepository.save(thread);
    }

}
