package com.ksptool.ql.biz.service.panel;

import com.ksptool.ql.biz.mapper.ModelRoleChatExampleRepository;
import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
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

import java.util.List;

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
    private ContentSecurityService contentSecurityService;

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
        query.setUserId(AuthService.getCurrentUserId());

        SimpleExample<ModelRolePo> simpleExample = SimpleExample.of(query)
                .like("name")
                .orderByDesc("updateTime");

        // 查询角色列表
        Page<ModelRolePo> pagePos = modelRoleRepository.findAll(simpleExample.get(),  dto.pageRequest().withSort(
                simpleExample.getSort()
        ));

        // 返回数据前先执行解密
        contentSecurityService.processList(pagePos.getContent(),false);

        // 创建分页视图
        PageableView<ListModelRoleItemVo> pageableView = new PageableView<>(pagePos,ListModelRoleItemVo.class);

        // 设置分页信息
        vo.setRoleList(pageableView);
        
        // 如果有选中的角色ID，则查询角色详情
        if (dto.getId() != null) {
            ModelRolePo rolePo = modelRoleRepository.findById(dto.getId()).orElse(null);
            if (rolePo != null) {

                // 返回数据前先执行解密
                contentSecurityService.process(rolePo,false);
                // 将角色详情映射到VO
                assign(rolePo, vo);
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

        // 获取当前用户ID
        Long currentUserId = AuthService.getCurrentUserId();

        // 新增角色
        if (createMode) {

            // 新增时检查名称是否已存在
            var query = new ModelRolePo();
            query.setUserId(AuthService.getCurrentUserId());
            query.setName(dto.getName());

            if (modelRoleRepository.count(Example.of(query)) > 0) {
                throw new BizException("角色名称已存在");
            }

            ModelRolePo insert = new ModelRolePo();
            assign(dto, insert);
            insert.setUserId(currentUserId);

            contentSecurityService.process(insert,true);
            return modelRoleRepository.save(insert);
        }

        ModelRolePo update = modelRoleRepository.findById(dto.getId()).orElseThrow(() -> new BizException("角色不存在!"));
        assign(dto,update);

        // 检查是否有权限修改该角色
        if (!update.getUserId().equals(currentUserId)) {
            throw new BizException("无权修改该角色");
        }

        // 更新时检查名称是否与其他角色重复
        if (modelRoleRepository.existsByNameAndIdNot(AuthService.getCurrentUserId(),dto.getName(), dto.getId())) {
            throw new BizException("角色名称已存在");
        }

        contentSecurityService.process(update,true);
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
            // 获取当前用户ID
            Long currentUserId = AuthService.getCurrentUserId();
            
            // 查询角色
            ModelRolePo rolePo = modelRoleRepository.findById(id).orElse(null);
            if (rolePo == null) {
                throw new BizException("角色不存在");
            }
            
            // 检查是否有权限删除该角色
            if (!rolePo.getUserId().equals(currentUserId)) {
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