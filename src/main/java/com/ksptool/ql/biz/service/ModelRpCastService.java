package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
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
    private ContentSecurityService contentSecurityService;


    /**
     * 根据ID获取当前用户拥有的模型角色
     * 查询指定ID的角色并解密内容
     * 
     * @param mRoleId 角色ID
     * @return 角色实体，如果未找到则返回null
     */
    public ModelRolePo getModelPlayRole(Long mRoleId) {
        if (mRoleId == null) {
            return null;
        }

        // 创建查询条件
        ModelRolePo query = new ModelRolePo();
        query.setId(mRoleId);
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        // 使用Example查询
        return modelRoleRepository.findOne(Example.of(query)).orElse(null);
    }



}
