package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
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

import java.util.List;


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
    private ContentSecurityService css;

    @Autowired
    private ModelRpHistoryRepository historyRepository;

    @Autowired
    private ModelRoleChatExampleRepository modelRoleChatExampleRepository;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private ModelRpSegmentRepository modelRpSegmentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * 创建新的RP对话存档
     * 会将同一角色下的其他存档设置为非激活状态，并创建一个新的激活状态的存档
     * 如果模型角色有首条消息，会同时创建首条AI消息
     *
     * @param modelPlayRoleCt 模型扮演的角色
     * @throws BizException 如果创建过程中出现错误
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewThread(ModelRolePo modelPlayRoleCt,String modeCode) throws BizException {

        Long currentPlayerId = AuthService.getCurrentPlayerId();

        if(currentPlayerId == null){
            throw new BizException("无法定位用户所登录的人物!");
        }

        // 解密用户角色和模型角色的加密字段
        String modelRoleFirstMsg = css.decryptForCurUser(modelPlayRoleCt.getFirstMessage());

        //取消该用户该角色下所有存档的激活状态
        threadRepository.setAllThreadActive(AuthService.getCurrentPlayerId(), modelPlayRoleCt.getId(), 0);
        entityManager.clear();

        //查询当前Player
        PlayerPo playerPo = playerRepository.findById(currentPlayerId)
                .orElseThrow(() -> new BizException("无法创建新存档:未找到用户所扮演的角色!"));

        //创建新存档
        ModelRpThreadPo thread = new ModelRpThreadPo();
        thread.setPlayer(playerPo);
        thread.setModelCode(modeCode);
        thread.setModelRole(modelPlayRoleCt);
        thread.setTitle(playerPo.getName() + "与" + modelPlayRoleCt.getName() + "的对话");
        thread.setActive(1);
        
        //加密存档内容
        css.encryptEntity(thread,AuthService.getCurrentUserId());
        threadRepository.save(thread);

        //如果模型角色有首条消息，创建首条AI消息
        if (StringUtils.isNotBlank(modelRoleFirstMsg)) {

            //准备Prompt
            var prompt = PreparedPrompt.prepare(modelRoleFirstMsg);
            prompt.setParameter("model",modelPlayRoleCt.getName());
            prompt.setParameter("user",playerPo.getName());

            ModelRpHistoryPo history = new ModelRpHistoryPo();
            history.setThread(thread);
            history.setType(1); // AI消息
            history.setRawContent(prompt.execute());
            history.setRpContent(modelRoleFirstMsg); // 这里可能需要通过RpHandler处理
            history.setSequence(1);
            css.encryptEntity(history,AuthService.getCurrentUserId());
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void activeThread(Long threadId) throws BizException {
        
        //查询指定ID的存档
        var query = new ModelRpThreadPo();
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        query.setId(threadId);

        ModelRpThreadPo thread = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("ThreadId无效!"));
                
        //获取该存档的角色ID
        Long roleId = thread.getModelRole().getId();

        //取消全部Thread的激活状态
        threadRepository.setAllThreadActive(AuthService.getCurrentPlayerId(), roleId, 0);
        entityManager.clear();

        //激活指定存档
        thread.setActive(1);
        threadRepository.save(thread);
    }

    /**
     * 根据会话ID获取当前玩家已激活的会话
     * @param threadId 会话ID
     * @return 不存在返回null
     * @throws BizException 错误抛出异常
     */
    @Transactional(readOnly = true)
    public ModelRpThreadPo getCurUserActiveThread(Long threadId) throws BizException {

        ModelRpThreadPo threadPo = threadRepository.getThreadWithRoleAndHistoriesById(threadId, AuthService.getCurrentPlayerId());

        if(threadPo == null) {
            return null;
        }

        if(threadPo.getActive() != 1){
            return null;
        }

        return threadPo;
    }

    /**
     * 查询该模型角色下是否还有示例对话 如果有示例对话将其附加到PrePrompt中
     * @param modelRoleId 模型角色ID
     * @param prompt 待附加的Prompt
     * @throws BizException 解密数据失败抛出
     */
    @Transactional(readOnly = true)
    public PreparedPrompt appendExamplePrompt(Long modelRoleId,PreparedPrompt prompt) throws BizException {

        //查询该模型角色是否还有示例对话
        List<ModelRoleChatExamplePo> exampleChatPos = modelRoleChatExampleRepository.getByModelRoleId(modelRoleId);

        if(exampleChatPos.isEmpty()){
            return prompt;
        }

        String unionStart = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_EXAMPLE_CHAT_START.getKey(), "#示例对话#\n\n");
        String unionEnd = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_EXAMPLE_CHAT_END.getKey(), "\n\n");
        String unionFinish = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_EXAMPLE_CHAT_FINISH.getKey(), "#示例对话结束#");

        var count = 0;
        //解析示例对话
        for(var item : exampleChatPos){
            if(StringUtils.isNotBlank(item.getContent())){
                prompt = prompt
                        .union(unionStart)
                        .union(css.decryptForCurUser(item.getContent()))
                        .union(unionEnd);
                count++;
            }
        }

        if(count > 0){
            prompt = prompt.union(unionFinish);
        }

        return prompt;
    }

    //解析模型主Prompt
    public PreparedPrompt createSystemPrompt(PlayerPo playerCt, ModelRolePo modelPlayRoleCt) {

        var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
        var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

        PreparedPrompt prompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
        prompt.setParameter("model", modelPlayRoleCt.getName());
        prompt.setParameter("user", "user");
        prompt.setParameter("userDesc", "");
        prompt.setParameter("modelDescription", css.decryptForCurUser(modelPlayRoleCt.getDescription()));
        prompt.setParameter("modelRoleSummary", css.decryptForCurUser(modelPlayRoleCt.getRoleSummary()));
        prompt.setParameter("modelScenario", css.decryptForCurUser(modelPlayRoleCt.getScenario()));

        if(playerCt != null) {
            prompt.setParameter("user", playerCt.getName());
            prompt.setParameter("userDesc", css.decryptForCurUser(playerCt.getDescription()));
        }
        return prompt;
    }

    /**
     * 创建新的加密用户历史消息记录
     * @param threadId 存档ID
     * @param content 消息内容
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ModelRpHistoryPo createNewRpUserHistory(Long threadId,String content) throws BizException {
        ModelRpHistoryPo userHistory = new ModelRpHistoryPo();
        var thread = new ModelRpThreadPo();
        thread.setId(threadId);
        userHistory.setThread(thread);
        userHistory.setType(0); // 用户消息
        userHistory.setRawContent(content);
        userHistory.setRpContent(content); // 这里可能需要通过RpHandler处理
        userHistory.setSequence(historyRepository.findMaxSequenceByThreadId(threadId) + 1);
        css.encryptEntity(userHistory,AuthService.getCurrentUserId());
        return historyRepository.save(userHistory);
    }


    /**
     * 创建新的开始片段
     * @param threadPt 会话存档PO
     */
    public void createStartSegment(ModelRpThreadPo threadPt) {
        ModelRpSegmentPo startSegment = new ModelRpSegmentPo();
        startSegment.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        startSegment.setThread(threadPt);
        startSegment.setSequence(0);
        startSegment.setContent(null);
        startSegment.setStatus(0); // 未读状态
        startSegment.setType(0); // 开始类型
        modelRpSegmentRepository.save(startSegment);
    }

    /**
     * 创建新的加密消息片段(数据)
     */
    public void createDataSegment(Long playerId,Long uid,Long threadId,String content,Integer seq) throws BizException {

        Integer nextSeq = seq;

        if(nextSeq == null){
            // 获取当前最大序号
            nextSeq = modelRpSegmentRepository.findMaxSequenceByThreadId(threadId) + 1;
        }

        // 数据类型 - 创建数据片段
        ModelRpSegmentPo dataSegment = new ModelRpSegmentPo();
        dataSegment.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
        var thread = new ModelRpThreadPo();
        thread.setId(threadId);
        dataSegment.setThread(thread);
        dataSegment.setSequence(nextSeq);
        dataSegment.setContent(content);
        dataSegment.setStatus(0); // 未读状态
        dataSegment.setType(1); // 数据类型
        css.encryptEntity(dataSegment,uid);
        modelRpSegmentRepository.save(dataSegment);
    }


}
