package com.ksptool.ql.biz.service.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.model.dto.SaveModelRoleChatExampleChatDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleChatExampleDto;
import com.ksptool.ql.biz.model.po.NpcChatExamplePo;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.vo.EditModelRoleChatExampleVo;
import com.ksptool.ql.biz.mapper.NpcChatExampleRepository;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ksptool.entities.Entities.assign;

@Service
public class PanelModelRoleChatExampleService {

    @Autowired
    private NpcChatExampleRepository repository;

    @Autowired
    private ContentSecurityService css;

    /**
     * 获取指定角色的所有对话示例
     * @param modelRoleId 角色ID
     * @return 对话示例列表
     * @throws BizException 业务异常
     */
    public List<EditModelRoleChatExampleVo> getExamplesByRoleId(Long modelRoleId) throws BizException {
        List<NpcChatExamplePo> examples = repository.getByNpcId(modelRoleId);
        
        List<EditModelRoleChatExampleVo> result = new ArrayList<>();
        for (NpcChatExamplePo po : examples) {
            EditModelRoleChatExampleVo vo = new EditModelRoleChatExampleVo();
            assign(po, vo);
            // 解密VO中的加密字段
            vo.setContent(css.decryptForCurUser(vo.getContent()));
            result.add(vo);
        }
        return result;
    }
    
    /**
     * 保存对话示例（包括新增、修改和删除）
     * @param dto 保存请求对象
     * @throws BizException 业务异常
     */
    @Transactional
    public void save(SaveModelRoleChatExampleDto dto) throws BizException {
        // 获取当前角色下的所有对话示例
        NpcChatExamplePo probe = new NpcChatExamplePo();
        probe.setNpc(Any.of().val("id",dto.getModelRoleId()).as(NpcPo.class));
        List<NpcChatExamplePo> existingExamples = repository.findAll(Example.of(probe));
        
        // 逐个处理聊天示例
        for (SaveModelRoleChatExampleChatDto chatDto : dto.getChatList()) {
            // 如果标记为删除且有ID，则删除
            if (chatDto.getRemove() != null && chatDto.getRemove() == 0 && chatDto.getId() != null) {
                repository.deleteById(chatDto.getId());
                continue;
            }
            
            // 如果有ID，表示更新已有示例
            if (chatDto.getId() != null) {
                NpcChatExamplePo existingPo = null;
                for (NpcChatExamplePo po : existingExamples) {
                    if (po.getId().equals(chatDto.getId())) {
                        existingPo = po;
                        break;
                    }
                }
                
                if (existingPo != null) {
                    existingPo.setContent(chatDto.getContent());
                    existingPo.setSortOrder(chatDto.getSortOrder());
                    existingPo.setUpdateTime(new Date());
                    // 加密内容
                    css.encryptEntity(existingPo);
                    repository.save(existingPo);
                }
                continue;
            }
            
            // 否则，创建新的示例
            NpcChatExamplePo newPo = new NpcChatExamplePo();
            newPo.setNpc(Any.of().val("id",dto.getModelRoleId()).as(NpcPo.class));
            newPo.setContent(chatDto.getContent());
            newPo.setSortOrder(chatDto.getSortOrder());
            // 加密内容
            css.encryptEntity(newPo);
            repository.save(newPo);
        }
    }

    /**
     * 根据ID删除对话示例
     * @param id 示例ID
     */
    public void removeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("示例ID不能为空");
        }
        repository.deleteById(id);
    }
}
