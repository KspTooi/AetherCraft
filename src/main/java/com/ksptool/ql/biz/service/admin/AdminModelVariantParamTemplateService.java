package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamRepository;
import com.ksptool.ql.biz.mapper.ModelVariantRepository;
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
    private ModelVariantParamRepository modelVariantParamRepository;

    @Autowired
    private ModelVariantRepository modelVariantRepository;

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
                .orElseThrow(() -> new BizException("模板不存在或无权限访问"));

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

        // 直接创建用户和玩家PO，避免重复代码
        UserPo user = new UserPo();
        user.setId(currentUserId);
        PlayerPo player = new PlayerPo();
        player.setId(currentPlayerId);

        ModelVariantParamTemplatePo template = null;
        
        if (dto.getTemplateId() != null) {
            // 编辑模式：使用Example查询现有模板
            ModelVariantParamTemplatePo query = new ModelVariantParamTemplatePo();
            query.setId(dto.getTemplateId());
            query.setUser(user);
            
            template = repository.findOne(Example.of(query))
                    .orElseThrow(() -> new BizException("模板不存在或无权限编辑"));
                    
            // 使用JPQL查询检查模板名称在当前用户范围内是否唯一（排除当前模板）
            if (repository.getTemplateByUserIdAndNameExcludeId(currentUserId, dto.getName(), dto.getTemplateId()).isPresent()) {
                throw new BizException("模板名称已存在");
            }
            
            // 更新模板名称
            template.setName(dto.getName());
            
            repository.save(template);
            return;
        }
        
        // 新增模式：使用JPQL查询检查模板名称在当前用户范围内是否唯一
        if (repository.getTemplateByUserIdAndNameExcludeId(currentUserId, dto.getName(), null).isPresent()) {
            throw new BizException("模板名称已存在");
        }
        
        // 创建新模板
        template = new ModelVariantParamTemplatePo();
        template.setName(dto.getName());
        template.setUser(user);
        template.setPlayer(player);
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

    /**
     * 应用模板为全局默认参数
     * @param dto 应用参数
     */
    @Transactional
    public void applyModelVariantParamTemplateToGlobal(ApplyModelVariantParamTemplateToGlobalDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();

        // 使用Example查询当前用户的模板
        ModelVariantParamTemplatePo templateQuery = new ModelVariantParamTemplatePo();
        templateQuery.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        templateQuery.setUser(userQuery);

        ModelVariantParamTemplatePo template = repository.findOne(Example.of(templateQuery))
                .orElseThrow(() -> new BizException("模板不存在或无权限应用"));

        // 使用Example查询验证目标模型变体是否存在
        ModelVariantPo modelVariantQuery = new ModelVariantPo();
        modelVariantQuery.setId(dto.getModelVariantId());
        ModelVariantPo modelVariant = modelVariantRepository.findOne(Example.of(modelVariantQuery))
                .orElseThrow(() -> new BizException("模型变体不存在"));

        // 通过关联关系获取模板的所有参数值
        List<ModelVariantParamTemplateValuePo> templateValues = template.getTemplateValues();
        
        if (templateValues == null || templateValues.isEmpty()) {
            throw new BizException("模板无参数值，无法应用");
        }

        // 第一步：清理目标模型变体下已有的全局默认参数
        // 删除该ModelVariantPo在ModelVariantParamPo表中所有user_id为NULL且player_id为NULL的记录
        List<ModelVariantParamPo> existingGlobalParams = modelVariantParamRepository
                .findByModelVariantIdAndUserIsNullAndPlayerIsNull(dto.getModelVariantId());
        if (!existingGlobalParams.isEmpty()) {
            modelVariantParamRepository.deleteAll(existingGlobalParams);
        }

        // 第二步：从模板创建新的全局默认参数记录
        // 将模板中的每个参数值，在ModelVariantParamPo表中为该ModelVariantPo创建一条新记录
        for (ModelVariantParamTemplateValuePo templateValue : templateValues) {
            ModelVariantParamPo newParam = new ModelVariantParamPo();
            newParam.setModelVariant(modelVariant);
            newParam.setParamKey(templateValue.getParamKey());
            newParam.setParamVal(templateValue.getParamVal());
            newParam.setType(templateValue.getType());
            newParam.setDescription(templateValue.getDescription());
            newParam.setSeq(templateValue.getSeq());
            // user和player为null表示全局参数
            newParam.setUser(null);
            newParam.setPlayer(null);
            modelVariantParamRepository.save(newParam);
        }
    }

    /**
     * 应用模板为个人参数
     * @param dto 应用参数
     */
    @Transactional
    public void applyModelVariantParamTemplateToPersonal(ApplyModelVariantParamTemplateToPersonalDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();
        Long currentPlayerId = AuthService.requirePlayerId();

        // 使用Example查询当前用户的模板
        ModelVariantParamTemplatePo templateQuery = new ModelVariantParamTemplatePo();
        templateQuery.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        templateQuery.setUser(userQuery);

        ModelVariantParamTemplatePo template = repository.findOne(Example.of(templateQuery))
                .orElseThrow(() -> new BizException("模板不存在或无权限应用"));

        // 使用Example查询验证目标模型变体是否存在
        ModelVariantPo modelVariantQuery = new ModelVariantPo();
        modelVariantQuery.setId(dto.getModelVariantId());
        ModelVariantPo modelVariant = modelVariantRepository.findOne(Example.of(modelVariantQuery))
                .orElseThrow(() -> new BizException("模型变体不存在"));

        // 直接创建用户和玩家PO，不查询数据库
        UserPo user = new UserPo();
        user.setId(currentUserId);
        PlayerPo player = new PlayerPo();
        player.setId(currentPlayerId);

        // 通过关联关系获取模板的所有参数值
        List<ModelVariantParamTemplateValuePo> templateValues = template.getTemplateValues();
        
        if (templateValues == null || templateValues.isEmpty()) {
            throw new BizException("模板无参数值，无法应用");
        }

        // 第一步：清理目标模型变体下该玩家已有的自定义参数
        // 删除该ModelVariantPo在ModelVariantParamPo表中所有user_id和player_id均匹配当前玩家的记录
        List<ModelVariantParamPo> existingPlayerParams = modelVariantParamRepository
                .findByModelVariantIdAndUserIdAndPlayerId(dto.getModelVariantId(), currentUserId, currentPlayerId);
        if (!existingPlayerParams.isEmpty()) {
            modelVariantParamRepository.deleteAll(existingPlayerParams);
        }

        // 第二步：从模板创建新的玩家自定义参数记录
        // 将模板中的每个参数值，在ModelVariantParamPo表中为该ModelVariantPo创建一条新记录
        for (ModelVariantParamTemplateValuePo templateValue : templateValues) {
            ModelVariantParamPo newParam = new ModelVariantParamPo();
            newParam.setModelVariant(modelVariant);
            newParam.setParamKey(templateValue.getParamKey());
            newParam.setParamVal(templateValue.getParamVal());
            newParam.setType(templateValue.getType());
            newParam.setDescription(templateValue.getDescription());
            newParam.setSeq(templateValue.getSeq());
            // 设置当前用户和玩家
            newParam.setUser(user);
            newParam.setPlayer(player);
            modelVariantParamRepository.save(newParam);
        }
    }

} 