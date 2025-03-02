package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelUserRoleRepository;
import com.ksptool.ql.biz.model.dto.ModelUserRoleQueryDto;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.model.vo.ModelUserRoleVo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private ModelUserRoleRepository modelUserRoleRepository;

    /**
     * 查询用户角色列表
     * @param queryDto 查询参数
     * @return 角色列表
     */
    public List<ModelUserRoleVo> findRoles(ModelUserRoleQueryDto queryDto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ModelUserRolePo> query = cb.createQuery(ModelUserRolePo.class);
        Root<ModelUserRolePo> root = query.from(ModelUserRolePo.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // 根据角色名称模糊查询
        if (StringUtils.hasText(queryDto.getName())) {
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
    public ModelUserRoleVo findById(Long id) {
        if (id == null) {
            return null;
        }
        
        Optional<ModelUserRolePo> optionalRole = modelUserRoleRepository.findById(id);
        if (optionalRole.isPresent()) {
            ModelUserRoleVo vo = new ModelUserRoleVo();
            assign(optionalRole.get(), vo);
            return vo;
        }
        
        return null;
    }
    
    /**
     * 保存角色
     * @param roleVo 角色信息
     * @return 保存后的角色
     */
    @Transactional
    public ModelUserRoleVo saveRole(ModelUserRoleVo roleVo) {
        ModelUserRolePo rolePo;
        
        if (roleVo.getId() != null) {
            // 更新
            Optional<ModelUserRolePo> optionalRole = modelUserRoleRepository.findById(roleVo.getId());
            if (optionalRole.isPresent()) {
                rolePo = optionalRole.get();
            } else {
                rolePo = new ModelUserRolePo();
            }
        } else {
            // 新增
            rolePo = new ModelUserRolePo();
        }
        
        // 设置属性
        assign(roleVo, rolePo);
        
        // 如果设置为默认角色，需要将其他角色设置为非默认
        if (roleVo.getIsDefault() != null && roleVo.getIsDefault() == 1) {
            modelUserRoleRepository.updateAllToNonDefault();
        }
        
        // 保存
        rolePo = modelUserRoleRepository.save(rolePo);
        
        // 返回结果
        ModelUserRoleVo result = new ModelUserRoleVo();
        assign(rolePo, result);
        return result;
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否成功
     */
    @Transactional
    public boolean deleteRole(Long id) {
        if (id == null) {
            return false;
        }
        
        // 检查是否为默认角色
        Optional<ModelUserRolePo> optionalRole = modelUserRoleRepository.findById(id);
        if (optionalRole.isPresent() && optionalRole.get().getIsDefault() == 1) {
            // 不允许删除默认角色
            return false;
        }
        
        modelUserRoleRepository.deleteById(id);
        return true;
    }
} 