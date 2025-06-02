package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelVariantRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateValueRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.model.dto.AdminToggleModelVariantDto;
import com.ksptool.ql.biz.model.dto.ApplyModelVariantParamTemplateDto;
import com.ksptool.ql.biz.model.dto.GetAdminModelVariantListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelVariantDto;
import com.ksptool.ql.biz.model.po.ModelVariantPo;
import com.ksptool.ql.biz.model.po.ModelVariantParamPo;
import com.ksptool.ql.biz.model.po.ModelVariantParamTemplatePo;
import com.ksptool.ql.biz.model.po.ModelVariantParamTemplateValuePo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import com.ksptool.ql.biz.model.vo.GetModelVariantListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.enums.AiModelVariantEnum;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.ArrayList;

import static com.ksptool.entities.Entities.assign;

@Service
public class ModelVariantService {

    @Autowired
    private ModelVariantRepository repository;

    @Autowired
    private ModelVariantParamRepository modelVariantParamRepository;

    @Autowired
    private ModelVariantParamTemplateRepository templateRepository;

    @Autowired
    private ModelVariantParamTemplateValueRepository templateValueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<GetModelVariantListVo> getClientModelVariantList(){
        // 查询所有启用的模型变体，按排序号和创建时间排序
        List<ModelVariantPo> enabledModels = repository.findEnabledModelVariants(1);
        
        List<GetModelVariantListVo> vos = new ArrayList<>();
        for (ModelVariantPo po : enabledModels) {
            GetModelVariantListVo vo = new GetModelVariantListVo();
            assign(po, vo);
            vos.add(vo);
        }
        
        return vos;
    }

    public RestPageableView<GetAdminModelVariantListVo> getModelVariantList(GetAdminModelVariantListDto dto) {
        Page<GetAdminModelVariantListVo> pageResult = repository.getAdminModelVariantList(
                dto.getKeyword(),
                dto.getEnabled(),
                dto.pageRequest()
        );

        return new RestPageableView<>(pageResult.getContent(), pageResult.getTotalElements());
    }

    public GetAdminModelVariantDetailsVo getModelVariantDetails(Long id) throws BizException {
        ModelVariantPo po = repository.findById(id)
                .orElseThrow(() -> new BizException("模型变体不存在"));

        GetAdminModelVariantDetailsVo vo = new GetAdminModelVariantDetailsVo();
        assign(po, vo);
        return vo;
    }

    @Transactional
    public void saveModelVariant(SaveAdminModelVariantDto dto) throws BizException {
        if (dto.getId() == null) {
            // 新增
            if (repository.existsByCode(dto.getCode())) {
                throw new BizException("模型代码已存在");
            }

            ModelVariantPo po = new ModelVariantPo();
            assign(dto, po);
            
            // 如果没有提供seq，则设为最大值+1
            if (po.getSeq() == null) {
                int maxSeq = repository.findAll().stream()
                        .mapToInt(p -> p.getSeq() != null ? p.getSeq() : 0)
                        .max()
                        .orElse(0);
                po.setSeq(maxSeq + 1);
            }
            
            repository.save(po);
            return;
        }

        // 编辑
        ModelVariantPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("要编辑的模型变体不存在"));

        if (repository.existsByCodeAndIdNot(dto.getCode(), dto.getId())) {
            throw new BizException("模型代码已存在");
        }

        assign(dto, po);
        repository.save(po);
    }

    @Transactional
    public void removeModelVariant(Long id) throws BizException {
        ModelVariantPo po = repository.findById(id)
                .orElseThrow(() -> new BizException("要删除的模型变体不存在"));

        repository.delete(po);
        entityManager.flush();
    }

    /**
     * 批量切换模型变体启用状态
     * 
     * @param dto 包含模型变体ID列表和目标启用状态的DTO
     * @throws BizException 当找不到模型变体时抛出异常
     */
    @Transactional
    public void toggleModelVariant(AdminToggleModelVariantDto dto) throws BizException {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BizException("模型变体ID列表不能为空");
        }
        
        if (dto.getEnabled() == null) {
            throw new BizException("启用状态不能为空");
        }
        
        // 批量查询所有要更新的模型变体
        List<ModelVariantPo> modelVariants = repository.findAllById(dto.getIds());
        
        if (modelVariants.size() != dto.getIds().size()) {
            throw new BizException("部分模型变体不存在，请检查ID列表");
        }
        
        // 批量更新启用状态
        for (ModelVariantPo po : modelVariants) {
            po.setEnabled(dto.getEnabled());
        }
        
        repository.saveAll(modelVariants);
    }

    /**
     * 校验系统内置模型变体
     * 检查数据库中是否存在所有系统内置模型变体，如果不存在则自动创建
     * 对已存在的模型变体不做任何修改
     */
    @Transactional
    public String validateSystemModelVariants() {
        int addedCount = 0;
        int existCount = 0;
        
        // 遍历所有系统内置的模型变体
        for (AiModelVariantEnum variant : AiModelVariantEnum.values()) {
            // 检查数据库中是否已存在该代码的模型变体
            if (repository.existsByCode(variant.getCode())) {
                existCount++;
                continue;
            }
            
            // 创建新的模型变体
            ModelVariantPo po = new ModelVariantPo();
            po.setCode(variant.getCode());
            po.setName(variant.getName());
            po.setType(0); // 默认为文本类型，可根据需要调整
            po.setSeries(variant.getSeries());
            po.setThinking(variant.getThinking());
            po.setScale(variant.getScale());
            po.setSpeed(variant.getSpeed());
            po.setIntelligence(variant.getIntelligence());
            po.setEnabled(1); // 默认启用
            
            // 设置排序号为最大值+1
            int maxSeq = repository.findAll().stream()
                    .mapToInt(p -> p.getSeq() != null ? p.getSeq() : 0)
                    .max()
                    .orElse(0);
            po.setSeq(maxSeq + addedCount + 1);
            
            repository.save(po);
            addedCount++;
        }
        
        if (addedCount > 0) {
            return String.format("校验完成，已添加 %d 个缺失的模型变体，已存在 %d 个模型变体", 
                    addedCount, existCount);
        }
        
        return String.format("校验完成，所有 %d 个系统模型变体均已存在", existCount);
    }


    /**
     * 根据模型代码获取模型变体Schema
     * @param modelCode 模型代码
     * @return 模型变体Schema，如果不存在则返回null
     */
    public ModelVariantSchema getModelSchema(String modelCode){
        ModelVariantPo query = new ModelVariantPo();
        query.setCode(modelCode);
        query.setEnabled(1);

        ModelVariantPo po = repository.findOne(Example.of(query)).orElse(null);
        if (po == null) {
            return null;
        }
        
        return po.getSchema();
    }

    /**
     * 根据模型代码获取模型变体Schema（必须存在）
     * @param modelCode 模型代码
     * @return 模型变体Schema
     * @throws BizException 当模型变体不存在时抛出异常
     */
    public ModelVariantSchema requireModelSchema(String modelCode) throws BizException {
        ModelVariantPo query = new ModelVariantPo();
        query.setCode(modelCode);
        query.setEnabled(1); //是否启用 0:禁用 1:启用

        ModelVariantPo po = repository.findOne(Example.of(query)).orElse(null);
        if (po == null) {
            throw new BizException("模型变体不存在或当前不可用: " + modelCode);
        }
        
        return po.getSchema();
    }

    /**
     * 应用参数模板到模型变体（支持批量应用和全局/个人参数选择）
     * @param dto 应用参数（包含模板ID、模型变体ID列表、应用范围）
     */
    @Transactional
    public void applyModelVariantParamTemplate(ApplyModelVariantParamTemplateDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();

        // 使用Example查询当前用户的模板
        ModelVariantParamTemplatePo templateQuery = new ModelVariantParamTemplatePo();
        templateQuery.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        templateQuery.setUser(userQuery);

        ModelVariantParamTemplatePo template = templateRepository.findOne(Example.of(templateQuery))
                .orElseThrow(() -> new BizException("模板不存在或无权限应用"));

        // 验证所有模型变体都存在
        List<ModelVariantPo> modelVariants = repository.findAllById(dto.getModelVariantIds());
        if (modelVariants.size() != dto.getModelVariantIds().size()) {
            throw new BizException("部分模型变体不存在，请检查ID列表");
        }

        // 获取模板的所有参数值
        List<ModelVariantParamTemplateValuePo> templateValues = templateValueRepository.findByTemplateIdOrderBySeq(dto.getTemplateId());

        // 根据global参数决定应用范围 应用范围：0=个人参数, 1=全局参数
        if (dto.getGlobal() == 1) {
            // 应用为全局参数
            List<ModelVariantParamPo> newParams = new ArrayList<>();
            
            for (Long modelVariantId : dto.getModelVariantIds()) {
                // 第一步：清理目标模型变体下已有的全局默认参数
                List<ModelVariantParamPo> existingGlobalParams = modelVariantParamRepository
                        .findByModelVariantIdAndUserIsNullAndPlayerIsNull(modelVariantId);
                if (!existingGlobalParams.isEmpty()) {
                    modelVariantParamRepository.deleteAll(existingGlobalParams);
                }
            }
            
            // 强制刷新删除操作到数据库
            entityManager.flush();
            
            for (Long modelVariantId : dto.getModelVariantIds()) {
                // 第二步：如果模板有参数值，从模板创建新的全局默认参数记录
                if (templateValues != null && !templateValues.isEmpty()) {
                    ModelVariantPo modelVariant = new ModelVariantPo();
                    modelVariant.setId(modelVariantId);

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
                        newParams.add(newParam);
                    }
                }
            }
            
            // 批量保存所有新参数
            if (!newParams.isEmpty()) {
                modelVariantParamRepository.saveAll(newParams);
            }
        }


        if (dto.getGlobal() == 0) {
            // 应用为个人参数
            Long currentPlayerId = AuthService.requirePlayerId();

            // 直接创建用户和玩家PO，不查询数据库
            UserPo user = new UserPo();
            user.setId(currentUserId);
            PlayerPo player = new PlayerPo();
            player.setId(currentPlayerId);

            // 检查每个模型变体的全局参数与模板的参数键是否一致
            for (Long modelVariantId : dto.getModelVariantIds()) {
                // 获取模型变体的全局参数
                List<ModelVariantParamPo> globalParams = modelVariantParamRepository
                        .findByModelVariantIdAndUserIsNullAndPlayerIsNull(modelVariantId);
                
                // 检查模板中的每个参数键是否在全局参数中存在
                if (templateValues != null && !templateValues.isEmpty()) {
                    for (ModelVariantParamTemplateValuePo templateValue : templateValues) {
                        boolean keyExists = false;
                        for (ModelVariantParamPo globalParam : globalParams) {
                            if (globalParam.getParamKey().equals(templateValue.getParamKey())) {
                                keyExists = true;
                                break;
                            }
                        }
                        if (!keyExists) {
                            throw new BizException("模板中存在全局参数中不存在的参数键: " + templateValue.getParamKey() + "，无法应用到模型变体");
                        }
                    }
                }
            }

            List<ModelVariantParamPo> newParams = new ArrayList<>();

            for (Long modelVariantId : dto.getModelVariantIds()) {
                // 第一步：清理目标模型变体下该玩家已有的自定义参数
                List<ModelVariantParamPo> existingPlayerParams = modelVariantParamRepository
                        .findByModelVariantIdAndUserIdAndPlayerId(modelVariantId, currentUserId, currentPlayerId);
                if (!existingPlayerParams.isEmpty()) {
                    modelVariantParamRepository.deleteAll(existingPlayerParams);
                }
            }
            
            // 强制刷新删除操作到数据库
            entityManager.flush();

            for (Long modelVariantId : dto.getModelVariantIds()) {
                // 第二步：如果模板有参数值，从模板创建新的玩家自定义参数记录
                if (templateValues != null && !templateValues.isEmpty()) {
                    ModelVariantPo modelVariant = new ModelVariantPo();
                    modelVariant.setId(modelVariantId);

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
                        newParams.add(newParam);
                    }
                }
            }
            
            // 批量保存所有新参数
            if (!newParams.isEmpty()) {
                modelVariantParamRepository.saveAll(newParams);
            }
        }
    }

}
