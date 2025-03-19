package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.mapper.ModelUserRoleRepository;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.po.ModelUserRolePo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

/**
 * 模型演员服务
 */
@Service
public class ModelRpCastService {

    @Autowired
    private ModelRoleRepository modelRoleRepository;

    @Autowired
    private ModelUserRoleRepository modelUserRoleRepository;

    @Autowired
    private ContentSecurityService contentSecurityService;


    /**
     * 获取用户当前扮演的角色
     * @param userId 用户ID
     * @return 用户当前扮演的角色，如果未找到则返回null
     */
    public ModelUserRolePo getUserPlayRole(Long userId) throws BizException {

        if (userId == null) {
            return null;
        }

        // 创建查询条件
        ModelUserRolePo query = new ModelUserRolePo();
        query.setUserId(userId);
        query.setIsDefault(1);

        ModelUserRolePo po = modelUserRoleRepository.findOne(Example.of(query)).orElse(null);
        contentSecurityService.process(po,false);
        return po;
    }

    /**
     * 根据ID获取当前用户拥有的模型角色
     * 查询指定ID的角色并解密内容
     * 
     * @param mRoleId 角色ID
     * @return 角色实体，如果未找到则返回null
     * @throws BizException 业务异常
     */
    public ModelRolePo getModelPlayRole(Long mRoleId) throws BizException {
        if (mRoleId == null) {
            return null;
        }

        // 创建查询条件
        ModelRolePo query = new ModelRolePo();
        query.setId(mRoleId);
        query.setUserId(AuthService.getCurrentUserId());

        // 使用Example查询
        ModelRolePo po = modelRoleRepository.findOne(Example.of(query)).orElse(null);
        
        // 解密角色内容
        contentSecurityService.process(po, false);
        return po;
    }



}
