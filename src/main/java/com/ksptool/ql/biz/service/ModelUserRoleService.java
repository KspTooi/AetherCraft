package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelUserRoleRepository;
import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import com.ksptool.ql.biz.model.dto.SaveModelUserRoleDto;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.model.vo.ModelUserRoleVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.SimpleExample;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * 用户角色服务
 */
@Service
public class ModelUserRoleService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private ModelUserRoleRepository rep;

    @Autowired
    private ContentSecurityService css;

    /**
     * 查询用户角色列表
     * @param dto 查询参数
     * @return 角色列表
     */
    public List<ModelUserRoleVo> getModelUserRoleList(ModelUserRoleQueryDto dto) throws BizException {
        ModelUserRolePo query = new ModelUserRolePo();
        query.setUserId(AuthService.getCurrentUserId());

        if(StringUtils.isNotBlank(dto.getName())){
            query.setName(dto.getName());
        }

        SimpleExample<ModelUserRolePo> example = SimpleExample.of(query)
                .like("name")
                .orderByDesc("updateTime");

        List<ModelUserRolePo> pos = rep.findAll(example.get(), example.getSort());
        List<ModelUserRoleVo> vos = as(pos, ModelUserRoleVo.class);
        
        // 解密VO中的加密字段
        for(ModelUserRoleVo vo : vos) {
            vo.setDescription(css.decryptForCurUser(vo.getDescription()));
            vo.setAvatarPath(css.decryptForCurUser(vo.getAvatarPath()));
        }
        
        return vos;
    }
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public ModelUserRoleVo getById(Long id) throws BizException {
        if (id == null) {
            return null;
        }
        
        Optional<ModelUserRolePo> optionalRole = rep.findById(id);
        if (optionalRole.isPresent()) {
            ModelUserRolePo po = optionalRole.get();
            ModelUserRoleVo vo = new ModelUserRoleVo();
            assign(po, vo);
            
            // 解密VO中的加密字段
            vo.setDescription(css.decryptForCurUser(vo.getDescription()));
            vo.setAvatarPath(css.decryptForCurUser(vo.getAvatarPath()));
            return vo;
        }
        
        return null;
    }
    
    /**
     * 保存角色
     * @param dto 角色信息
     * @return 保存后的角色ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = BizException.class)
    public Long saveModelUserRole(SaveModelUserRoleDto dto) throws BizException {

        var createMode = dto.getId() == null;
        Long currentUserId = AuthService.getCurrentUserId();

        ModelUserRolePo modelUserRolePo = null;

        if(createMode){
            //新增角色时检查名称是否已存在
            var query = new ModelUserRolePo();
            query.setUserId(AuthService.getCurrentUserId());
            query.setName(dto.getName());

            if(rep.count(Example.of(query)) > 0){
                throw new BizException("角色名称:"+dto.getName()+"被占用");
            }

            modelUserRolePo = new ModelUserRolePo();
            assign(dto, modelUserRolePo);
        }

        if(!createMode){
            //获取现有角色
            var query = new ModelUserRolePo();
            query.setId(dto.getId());
            query.setUserId(currentUserId);
            modelUserRolePo = rep.findOne(Example.of(query)).orElseThrow(() -> new BizException("角色不存在"));
            
            //更新角色时检查名称是否与其他角色重复
            if(!StringUtils.equals(modelUserRolePo.getName(), dto.getName())) {
                if(rep.countByUserIdAndNameAndIdNot(currentUserId, dto.getName(), dto.getId()) > 0) {
                    throw new BizException("角色名称:" + dto.getName() + "被占用");
                }
            }
            
            assign(dto,modelUserRolePo);
        }

        //用户将当前更新的角色设为了默认
        if(dto.getIsDefault() == 1){
            rep.updateAllToNonDefault();
            // 清除实体管理器缓存，确保后续操作使用最新的数据库状态
            entityManager.clear();
        }

        //保存前加密角色为密文
        css.encryptEntity(modelUserRolePo);
        rep.save(modelUserRolePo);

        //检查用户现在是否有默认角色
        var query = new ModelUserRolePo();
        query.setUserId(currentUserId);
        query.setIsDefault(1);

        if(rep.count(Example.of(query)) < 1){
            //查询该用户的第一个角色 将其设为默认
            query = new ModelUserRolePo();
            query.setUserId(currentUserId);

            List<ModelUserRolePo> pos = rep.findAll(Example.of(query));

            if(pos.isEmpty()){
                throw new BizException("用户没有任何角色!");
            }

            ModelUserRolePo first = pos.getFirst();
            first.setIsDefault(1);
            rep.save(first);
        }

        // 返回保存后的角色ID
        return modelUserRolePo.getId();
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否成功
     * @throws BizException 删除失败时抛出异常
     */
    @Transactional
    public boolean removeModelUserRole(Long id) throws BizException {
        if (id == null) {
            throw new BizException("角色ID不能为空");
        }
        
        // 检查是否为默认角色
        Optional<ModelUserRolePo> optionalRole = rep.findById(id);
        if (optionalRole.isEmpty()) {
            throw new BizException("角色不存在");
        }
        
        if (optionalRole.get().getIsDefault() == 1) {
            // 不允许删除默认角色
            throw new BizException("不允许删除默认角色");
        }
        
        try {
            rep.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new BizException("删除角色失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 为用户创建默认角色（如果用户没有任何角色）
     * @param userId 用户ID
     * @return 创建的角色ID，如果用户已有角色则返回null
     * @throws BizException 创建角色失败时抛出异常
     */
    @Transactional
    public void createDefaultUserRole(Long userId) throws BizException {

        if (userId == null) {
            throw new BizException("用户ID不能为空");
        }
        
        // 查询用户是否已有角色
        ModelUserRolePo query = new ModelUserRolePo();
        query.setUserId(userId);
        Example<ModelUserRolePo> example = Example.of(query);
        List<ModelUserRolePo> userRoles = rep.findAll(example);
        
        // 如果用户已有角色，则不创建新角色
        if (!userRoles.isEmpty()) {
            return;
        }
        
        // 创建默认角色
        ModelUserRolePo defaultRole = new ModelUserRolePo();
        defaultRole.setName("用户");
        defaultRole.setDescription("");
        defaultRole.setUserId(userId);
        defaultRole.setAvatarPath(null);
        defaultRole.setSortOrder(0);
        defaultRole.setIsDefault(1); // 设置为默认角色
        
        try {
            css.encryptEntity(defaultRole);
            // 保存角色
            rep.save(defaultRole);
        } catch (Exception e) {
            throw new BizException("创建默认角色失败: " + e.getMessage(), e);
        }
    }
} 