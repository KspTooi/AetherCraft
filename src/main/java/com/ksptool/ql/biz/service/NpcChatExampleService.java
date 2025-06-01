package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.NpcChatExampleRepository;
import com.ksptool.ql.biz.mapper.NpcRepository;
import com.ksptool.ql.biz.model.dto.SaveNpcChatExampleDto;
import com.ksptool.ql.biz.model.po.NpcChatExamplePo;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetNpcChatExampleListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class NpcChatExampleService {

    @Autowired
    private NpcChatExampleRepository repository;

    @Autowired
    private NpcRepository npcRepository;

    @Autowired
    private ContentSecurityService css;


    public List<GetNpcChatExampleListVo> getModelRoleList(Long npcId) throws BizException {

        //查询NPC
        var npcQuery = new NpcPo();
        npcQuery.setId(npcId);
        npcQuery.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        NpcPo npcPo = npcRepository.findOne(Example.of(npcQuery))
                .orElseThrow(() -> new BizException("NPC不存在或无访问权限"));

        var query = new NpcChatExamplePo();
        query.setNpc(npcPo);
        
        List<NpcChatExamplePo> pos = repository.getByNpcId(npcPo.getId());
        List<GetNpcChatExampleListVo> vos = new ArrayList<>();
        for(var po : pos){
            var vo = as(po,GetNpcChatExampleListVo.class);
            vo.setContent(css.decryptForCurUser(vo.getContent()));
            vos.add(vo);
        }
        return vos;
    }

    @Transactional
    public void saveNpcChatExample(@RequestBody @Valid SaveNpcChatExampleDto dto) throws BizException {
        //查询NPC
        var npcQuery = new NpcPo();
        npcQuery.setId(dto.getNpcId());
        npcQuery.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        NpcPo npcPo = npcRepository.findOne(Example.of(npcQuery))
                .orElseThrow(() -> new BizException("NPC不存在或无访问权限"));

        for(var example : dto.getExamples()){

            //新增
            if(example.getId() == null){
                var insert = new NpcChatExamplePo();
                assign(example,insert);
                insert.setNpc(npcPo);
                insert.setSortOrder(repository.getNextOrder(npcPo.getId()));
                css.encryptEntity(insert);
                repository.save(insert);
                continue;
            }

            //修改
            var query = new NpcChatExamplePo();
            query.setId(example.getId());
            query.setNpc(npcPo);

            NpcChatExamplePo po = repository.findOne(Example.of(query))
                    .orElseThrow(() -> new BizException("NPC对话示例不存在或无访问权限"));

            po.setContent(example.getContent());
            po.setSortOrder(example.getSortOrder());
            css.encryptEntity(po);
            repository.save(po);
        }

    }

    @Transactional
    public void removeNpcChatExample(Long exampleId) throws BizException {

        NpcChatExamplePo po = repository.findById(exampleId)
                .orElseThrow(() -> new BizException("对话示例不存在或无访问权限"));
        
        if(!Objects.equals(po.getNpc().getPlayer().getId(), AuthService.getCurrentPlayerId())){
            throw new BizException("无访问权限");
        }

        repository.delete(po);
    }

}
