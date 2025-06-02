package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateValueRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.*;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.assign;

import java.util.List;

@Service
public class AdminModelVariantParamTemplateService {

    @Autowired
    private ModelVariantParamTemplateRepository repository;

    @Autowired
    private ModelVariantParamTemplateValueRepository templateValueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * 查询参数模板列表，包含当前用户的模板（不包含模板值）
     * @param dto 查询参数
     * @return 模板列表
     */
    public RestPageableView<GetModelVariantParamTemplateListVo> getModelVariantParamTemplateList(GetModelVariantParamTemplateListDto dto) throws AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();

        // 使用dto.pageRequest()获取分页对象
        Page<GetModelVariantParamTemplateListVo> page = repository.getModelVariantParamTemplateListByUser(
                currentUserId, dto.getKeyword(), dto.pageRequest());

        return new RestPageableView<>(page.getContent(), page.getTotalElements());
    }

    /**
     * 查询模板详情（不包含模板值，模板值由另一个控制器管理）
     * @param dto 查询参数
     * @return 模板详情
     */
    public GetModelVariantParamTemplateDetailsVo getModelVariantParamTemplateDetails(GetModelVariantParamTemplateDetailsDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();

        // 使用Example查询当前用户的模板
        ModelVariantParamTemplatePo query = new ModelVariantParamTemplatePo();
        query.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        query.setUser(userQuery);

        ModelVariantParamTemplatePo template = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("模板不存在或无权限查看"));

        GetModelVariantParamTemplateDetailsVo vo = new GetModelVariantParamTemplateDetailsVo();
        assign(template, vo);
        return vo;
    }

    /**
     * 保存参数模板（仅模板基本信息，不包含模板值）
     * @param dto 保存参数
     */
    @Transactional
    public void saveModelVariantParamTemplate(SaveModelVariantParamTemplateDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();
        Long currentPlayerId = AuthService.requirePlayerId();

        ModelVariantParamTemplatePo template = null;
        if (dto.getTemplateId() != null) {
            // 编辑模式：查询现有模板
            ModelVariantParamTemplatePo existingQuery = new ModelVariantParamTemplatePo();
            existingQuery.setId(dto.getTemplateId());
            UserPo userQuery = new UserPo();
            userQuery.setId(currentUserId);
            existingQuery.setUser(userQuery);
            
            template = repository.findOne(Example.of(existingQuery))
                    .orElseThrow(() -> new BizException("模板不存在或无权限编辑"));
        }

        // 检查名称唯一性
        ModelVariantParamTemplatePo nameQuery = new ModelVariantParamTemplatePo();
        nameQuery.setName(dto.getName());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        nameQuery.setUser(userQuery);

        if (repository.findOne(Example.of(nameQuery)).isPresent()) {
            // 如果是编辑模式，需要排除当前模板
            if (dto.getTemplateId() != null) {
                ModelVariantParamTemplatePo found = repository.findOne(Example.of(nameQuery)).get();
                if (!found.getId().equals(dto.getTemplateId())) {
                    throw new BizException("模板名称已存在");
                }
            }
            if (dto.getTemplateId() == null) {
                throw new BizException("模板名称已存在");
            }
        }

        if (template == null) {
            // 新增模式
            template = new ModelVariantParamTemplatePo();
            
            // 直接创建用户和玩家PO，不查询数据库
            UserPo user = new UserPo();
            user.setId(currentUserId);
            PlayerPo player = new PlayerPo();
            player.setId(currentPlayerId);
            
            template.setUser(user);
            template.setPlayer(player);
        }

        template.setName(dto.getName());
        repository.save(template);
    }

    /**
     * 删除参数模板
     * @param dto 删除参数
     */
    @Transactional
    public void removeModelVariantParamTemplate(CommonIdDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();

        // 使用Example查询当前用户的模板
        ModelVariantParamTemplatePo query = new ModelVariantParamTemplatePo();
        query.setId(dto.getId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        query.setUser(userQuery);

        ModelVariantParamTemplatePo template = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("模板不存在或无权限删除"));

        // 删除模板的所有关联值
        ModelVariantParamTemplateValuePo valueQuery = new ModelVariantParamTemplateValuePo();
        valueQuery.setTemplate(template);
        List<ModelVariantParamTemplateValuePo> templateValues = templateValueRepository.findAll(Example.of(valueQuery));
        if (!templateValues.isEmpty()) {
            templateValueRepository.deleteAll(templateValues);
        }

        // 删除模板
        repository.delete(template);
    }

    @Transactional
    public void copyModelVariantParamTemplate(long id) throws BizException, AuthException {
        Long currentUserId = AuthService.getCurrentUserId();

        // 1. 查询源模板
        ModelVariantParamTemplatePo query = new ModelVariantParamTemplatePo();
        query.setId(id);
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        query.setUser(userQuery);

        ModelVariantParamTemplatePo originalTemplate = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("源模板不存在或无权限复制"));

        // 2. 创建新模板
        ModelVariantParamTemplatePo newTemplate = new ModelVariantParamTemplatePo();
        assign(originalTemplate, newTemplate);
        newTemplate.setId(null); // 设置ID为null，让JPA生成新的ID
        newTemplate.setName(originalTemplate.getName() + "_副本");

        // 检查复制后的名称唯一性
        ModelVariantParamTemplatePo nameCheckQuery = new ModelVariantParamTemplatePo();
        nameCheckQuery.setName(newTemplate.getName());
        nameCheckQuery.setUser(userQuery); // 关联当前用户，检查用户下的名称唯一性
        if (repository.findOne(Example.of(nameCheckQuery)).isPresent()) {
             throw new BizException("复制后的模板名称已存在，请修改");
        }

        UserPo currentUserPo = new UserPo();
        currentUserPo.setId(currentUserId);
        newTemplate.setUser(currentUserPo);

        PlayerPo currentPlayerPo = new PlayerPo();
        currentPlayerPo.setId(AuthService.requirePlayerId());
        newTemplate.setPlayer(currentPlayerPo);

        repository.save(newTemplate);

        // 3. 复制模板下的所有参数值
        ModelVariantParamTemplateValuePo valueQuery = new ModelVariantParamTemplateValuePo();
        valueQuery.setTemplate(originalTemplate);
        List<ModelVariantParamTemplateValuePo> originalValues = templateValueRepository.findAll(Example.of(valueQuery));

        List<ModelVariantParamTemplateValuePo> newValues = new java.util.ArrayList<>();
        for (ModelVariantParamTemplateValuePo originalValue : originalValues) {
            ModelVariantParamTemplateValuePo newValue = new ModelVariantParamTemplateValuePo();
            assign(originalValue, newValue);
            newValue.setId(null); // 设置ID为null，让JPA生成新的ID
            newValue.setTemplate(newTemplate); // 关联到新的模板
            newValues.add(newValue);
        }

        if (!newValues.isEmpty()) {
            templateValueRepository.saveAll(newValues);
        }
    }

} 