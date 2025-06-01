package com.ksptool.ql.biz.service.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.NpcChatExampleRepository;
import com.ksptool.ql.biz.mapper.NpcRepository;
import com.ksptool.ql.biz.model.dto.SaveNpcDto;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.assign;

/**
 * 模型角色管理服务
 */
@Service
public class PanelNpcService {

    @Autowired
    private NpcRepository npcRepository;
    
    @Autowired
    private NpcChatExampleRepository npcChatExampleRepository;

    @Autowired
    private ContentSecurityService css;


    /**
     * 保存模型角色
     * 
     * @param dto 保存参数
     * @return 保存结果
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = BizException.class)
    public NpcPo saveNpc(SaveNpcDto dto) throws BizException {

        var createMode = dto.getId() == null;

        // 新增角色
        if (createMode) {

            // 新增时检查名称是否已存在
            var query = new NpcPo();
            query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            query.setName(dto.getName());

            if (npcRepository.count(Example.of(query)) > 0) {
                throw new BizException("NPC名称已存在");
            }

            NpcPo insert = new NpcPo();
            assign(dto, insert);
            insert.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            insert.setActive(0);

            css.encryptEntity(insert);
            return npcRepository.save(insert);
        }

        //编辑角色
        NpcPo update = npcRepository.findById(dto.getId()).orElseThrow(() -> new BizException("角色不存在!"));
        assign(dto,update);

        // 检查是否有权限修改该角色
        if (!update.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
            throw new BizException("无权修改该角色");
        }

        // 更新时检查名称是否与其他角色重复
        if (npcRepository.existsByNameAndIdNot(AuthService.getCurrentPlayerId(),dto.getName(), dto.getId())) {
            throw new BizException("NPC名称已存在");
        }

        css.encryptEntity(update);
        npcRepository.save(update);
        return update;
    }
    
    /**
     * 删除模型角色
     * 
     * @param id 角色ID
     * @throws BizException 业务异常
     */
    @Transactional
    public void removeNpc(Long id) throws BizException {
        if (id == null) {
            throw new BizException("角色ID不能为空");
        }
        
        try {

            // 查询角色
            NpcPo rolePo = npcRepository.findById(id).orElse(null);
            if (rolePo == null) {
                throw new BizException("角色不存在");
            }
            
            // 检查是否有权限删除该角色
            if (!rolePo.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
                throw new BizException("无权删除该角色");
            }
            
            // 先删除角色相关的对话示例
            npcChatExampleRepository.removeByNpcId(id);
            
            // 删除角色
            npcRepository.deleteById(id);
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            throw new BizException("删除角色失败：" + e.getMessage(), e);
        }
    }
} 