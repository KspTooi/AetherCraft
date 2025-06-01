package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


/**
 * 模型RP剧本与场景服务
 */
@Service
public class NpcScriptService {

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private NpcChatExampleRepository npcChatExampleRepository;

    @Autowired
    private GlobalConfigService globalConfigService;


    /**
     * 查询该模型角色下是否还有示例对话 如果有示例对话将其附加到PrePrompt中
     * @param npcId 模型角色ID
     * @param prompt 待附加的Prompt
     * @throws BizException 解密数据失败抛出
     */
    @Transactional(readOnly = true)
    public PreparedPrompt appendExamplePrompt(Long npcId,PreparedPrompt prompt) throws BizException {

        //查询该NPC是否还有示例对话
        List<NpcChatExamplePo> exampleChatPos = npcChatExampleRepository.getByNpcId(npcId);

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
    public PreparedPrompt createSystemPrompt(PlayerPo playerCt, NpcPo npcCt) {

        var mainPromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_MAIN.getKey());
        var rolePromptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_RP_PROMPT_ROLE.getKey());

        PreparedPrompt prompt = PreparedPrompt.prepare(mainPromptTemplate).union(rolePromptTemplate);
        prompt.setParameter("npc", npcCt.getName());
        prompt.setParameter("player", "user");

        prompt.setParameter("playerDesc", "");
        prompt.setParameter("npcDescription", css.decryptForCurUser(npcCt.getDescription()));
        prompt.setParameter("npcRoleSummary", css.decryptForCurUser(npcCt.getRoleSummary()));
        prompt.setParameter("npcScenario", css.decryptForCurUser(npcCt.getScenario()));

        if(playerCt != null) {
            prompt.setParameter("player", playerCt.getName());
            prompt.setParameter("playerDesc", css.decryptForCurUser(playerCt.getDescription()));
        }
        return prompt;
    }







}
