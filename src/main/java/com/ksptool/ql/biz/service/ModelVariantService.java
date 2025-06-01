package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelVariantRepository;
import com.ksptool.ql.biz.model.dto.AdminToggleModelVariantDto;
import com.ksptool.ql.biz.model.dto.GetAdminModelVariantListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelVariantDto;
import com.ksptool.ql.biz.model.po.ModelVariantPo;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import com.ksptool.ql.biz.model.vo.GetModelVariantListVo;
import com.ksptool.ql.commons.enums.AiModelVariantEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

import static com.ksptool.entities.Entities.assign;

@Service
public class ModelVariantService {

    @Autowired
    private ModelVariantRepository repository;


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
        Page<ModelVariantPo> pageResult = repository.getAdminModelVariantList(
                dto.getKeyword(),
                dto.getEnabled(),
                dto.pageRequest()
        );

        List<GetAdminModelVariantListVo> vos = new ArrayList<>();
        for (ModelVariantPo po : pageResult.getContent()) {
            GetAdminModelVariantListVo vo = new GetAdminModelVariantListVo();
            assign(po, vo);
            vos.add(vo);
        }

        return new RestPageableView<>(vos, pageResult.getTotalElements());
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



}
