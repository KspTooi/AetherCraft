package com.ksptool.ql.biz.service.panel;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ModelRoleChatExampleRepository;
import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.ListModelRoleItemVo;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.assign;

/**
 * 模型角色管理服务
 */
@Service
public class PanelModelRoleService {

    @Autowired
    private ModelRoleRepository modelRoleRepository;
    
    @Autowired
    private ModelRoleChatExampleRepository modelRoleChatExampleRepository;
    @Autowired
    private ContentSecurityService css;

    /**
     * 获取模型角色列表视图
     * 
     * @param dto 查询参数
     * @return 模型角色列表视图
     */
    @Transactional(readOnly = true)
    public ListModelRoleVo getListView(ListModelRoleDto dto) throws BizException {
        // 创建视图对象
        ListModelRoleVo vo = new ListModelRoleVo();
        
        // 设置DTO中的参数回显
        vo.setId(dto.getId());
        vo.setKeyword(dto.getKeyword());

        ModelRolePo query = new ModelRolePo();
        query.setName(dto.getKeyword());
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        SimpleExample<ModelRolePo> simpleExample = SimpleExample.of(query)
                .like("name")
                .orderByDesc("updateTime");

        // 查询角色列表
        Page<ModelRolePo> pagePos = modelRoleRepository.findAll(simpleExample.get(),  dto.pageRequest().withSort(
                simpleExample.getSort()
        ));
        
        // 创建分页视图
        PageableView<ListModelRoleItemVo> pageableView = new PageableView<>(pagePos,ListModelRoleItemVo.class);
        
        // 解密列表中的加密字段
        for(ListModelRoleItemVo item : pageableView.getRows()) {
            item.setDescription(css.decryptForCurUser(item.getDescription()));
            item.setAvatarPath(css.decryptForCurUser(item.getAvatarPath()));
        }

        // 设置分页信息
        vo.setRoleList(pageableView);
        
        // 如果有选中的角色ID，则查询角色详情
        if (dto.getId() != null) {
            ModelRolePo rolePo = modelRoleRepository.findById(dto.getId()).orElse(null);
            if (rolePo != null) {
                // 将角色详情映射到VO
                assign(rolePo, vo);
                // 解密选中角色的加密字段
                vo.setDescription(css.decryptForCurUser(vo.getDescription()));
                vo.setAvatarPath(css.decryptForCurUser(vo.getAvatarPath()));
                vo.setRoleSummary(css.decryptForCurUser(vo.getRoleSummary()));
                vo.setScenario(css.decryptForCurUser(vo.getScenario()));
                vo.setFirstMessage(css.decryptForCurUser(vo.getFirstMessage()));
                vo.setTags(css.decryptForCurUser(vo.getTags()));
            }
        }
        
        return vo;
    }


    /**
     * 保存模型角色
     * 
     * @param dto 保存参数
     * @return 保存结果
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = BizException.class)
    public ModelRolePo saveModelRole(SaveModelRoleDto dto) throws BizException {

        var createMode = dto.getId() == null;

        // 新增角色
        if (createMode) {

            // 新增时检查名称是否已存在
            var query = new ModelRolePo();
            query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            query.setName(dto.getName());

            if (modelRoleRepository.count(Example.of(query)) > 0) {
                throw new BizException("角色名称已存在");
            }

            ModelRolePo insert = new ModelRolePo();
            assign(dto, insert);
            insert.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

            css.encryptEntity(insert);
            return modelRoleRepository.save(insert);
        }

        //编辑角色
        ModelRolePo update = modelRoleRepository.findById(dto.getId()).orElseThrow(() -> new BizException("角色不存在!"));
        assign(dto,update);

        // 检查是否有权限修改该角色
        if (!update.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
            throw new BizException("无权修改该角色");
        }

        // 更新时检查名称是否与其他角色重复
        if (modelRoleRepository.existsByNameAndIdNot(AuthService.getCurrentPlayerId(),dto.getName(), dto.getId())) {
            throw new BizException("角色名称已存在");
        }

        css.encryptEntity(update);
        modelRoleRepository.save(update);
        return update;
    }
    
    /**
     * 删除模型角色
     * 
     * @param id 角色ID
     * @throws BizException 业务异常
     */
    @Transactional
    public void removeModelRole(Long id) throws BizException {
        if (id == null) {
            throw new BizException("角色ID不能为空");
        }
        
        try {

            // 查询角色
            ModelRolePo rolePo = modelRoleRepository.findById(id).orElse(null);
            if (rolePo == null) {
                throw new BizException("角色不存在");
            }
            
            // 检查是否有权限删除该角色
            if (!rolePo.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
                throw new BizException("无权删除该角色");
            }
            
            // 先删除角色相关的对话示例
            modelRoleChatExampleRepository.removeByModelRoleId(id);
            
            // 删除角色
            modelRoleRepository.deleteById(id);
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw (BizException) e;
            }
            throw new BizException("删除角色失败：" + e.getMessage(), e);
        }
    }
} 