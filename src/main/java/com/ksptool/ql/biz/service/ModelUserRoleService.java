package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelUserRoleRepository;
import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import com.ksptool.ql.biz.model.dto.SaveModelUserRoleDto;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.model.vo.ModelUserRoleVo;
import com.ksptool.ql.commons.exception.BizException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * 查询用户角色列表
     * @param queryDto 查询参数
     * @return 角色列表
     */
    public List<ModelUserRoleVo> getModelUserRoleList(ModelUserRoleQueryDto queryDto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ModelUserRolePo> query = cb.createQuery(ModelUserRolePo.class);
        Root<ModelUserRolePo> root = query.from(ModelUserRolePo.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // 根据角色名称模糊查询
        if (StringUtils.isNotBlank(queryDto.getName())) {
            predicates.add(cb.like(root.get("name"), "%" + queryDto.getName() + "%"));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("sortOrder")), cb.asc(root.get("id")));
        
        TypedQuery<ModelUserRolePo> typedQuery = entityManager.createQuery(query);
        List<ModelUserRolePo> roles = typedQuery.getResultList();
        
        // 转换为Vo
        List<ModelUserRoleVo> roleVos = new ArrayList<>();
        for (ModelUserRolePo role : roles) {
            ModelUserRoleVo vo = new ModelUserRoleVo();
            assign(role, vo);
            roleVos.add(vo);
        }
        
        return roleVos;
    }
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public ModelUserRoleVo getById(Long id) {

        if (id == null) {
            return null;
        }
        
        Optional<ModelUserRolePo> optionalRole = rep.findById(id);
        if (optionalRole.isPresent()) {
            ModelUserRoleVo vo = new ModelUserRoleVo();
            assign(optionalRole.get(), vo);
            return vo;
        }
        
        return null;
    }
    
    /**
     * 保存角色
     * @param roleDto 角色信息
     * @return 保存后的角色ID
     * @throws BizException 业务异常
     */
    @Transactional
    public Long saveModelUserRole(SaveModelUserRoleDto roleDto) throws BizException {
        // 新增角色时检查名称是否已存在
        if (roleDto.getId() == null) {
            ModelUserRolePo existingRole = rep.findByName(roleDto.getName());
            if (existingRole != null) {
                throw new BizException("角色名称 '" + roleDto.getName() + "' 已存在，请使用其他名称");
            }
        }
        
        // 更新角色时检查名称是否与其他角色重复
        if (roleDto.getId() != null) {
            ModelUserRolePo existingRole = rep.findByNameAndIdNot(roleDto.getName(), roleDto.getId());
            if (existingRole != null) {
                throw new BizException("角色名称 '" + roleDto.getName() + "' 已存在，请使用其他名称");
            }
        }
        
        ModelUserRolePo rolePo = new ModelUserRolePo();
        
        // 如果是更新操作，获取现有角色
        if (roleDto.getId() != null) {
            Optional<ModelUserRolePo> optionalRole = rep.findById(roleDto.getId());
            if (optionalRole.isPresent()) {
                rolePo = optionalRole.get();
            }
        }
        
        // 设置属性
        assign(roleDto, rolePo);
        rolePo.setUserId(AuthService.getCurrentUserId());
        
        // 是否需要设置为默认角色
        if(roleDto.getIsDefault() == 1){
            rep.updateAllToNonDefault();
            // 清除实体管理器缓存，确保后续操作使用最新的数据库状态
            entityManager.clear();
        }

        // 保存角色
        rolePo = rep.save(rolePo);
        
        // 检查用户是否有默认角色
        ModelUserRolePo query = new ModelUserRolePo();
        query.setUserId(rolePo.getUserId());
        query.setIsDefault(1);
        if (rep.findOne(Example.of(query)).isEmpty()) {
            // 查询该用户的第一个角色
            query.setIsDefault(null);
            List<ModelUserRolePo> userRoles = rep.findAll(Example.of(query));
            if (!userRoles.isEmpty()) {
                ModelUserRolePo firstRole = userRoles.getFirst();
                firstRole.setIsDefault(1);
                rep.save(firstRole);
            }
        }
        
        // 返回保存后的角色ID
        return rolePo.getId();
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
        if (!optionalRole.isPresent()) {
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
        defaultRole.setName("");
        defaultRole.setDescription("");
        defaultRole.setUserId(userId);
        defaultRole.setAvatarPath(null);
        defaultRole.setSortOrder(0);
        defaultRole.setIsDefault(1); // 设置为默认角色
        
        try {
            // 保存角色
            rep.save(defaultRole);
        } catch (Exception e) {
            throw new BizException("创建默认角色失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户当前扮演的角色
     * @param userId 用户ID
     * @return 用户当前扮演的角色，如果未找到则返回null
     */
    public ModelUserRolePo getUserPlayRole(Long userId) {

        if (userId == null) {
            return null;
        }

        // 创建查询条件
        ModelUserRolePo query = new ModelUserRolePo();
        query.setUserId(userId);
        query.setIsDefault(1);
        return rep.findOne(Example.of(query)).orElse(null);
    }
} 