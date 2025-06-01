package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateRepository;
import com.ksptool.ql.biz.mapper.ModelVariantParamTemplateValueRepository;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamTemplateValueListDto;
import com.ksptool.ql.biz.model.dto.SaveModelVariantParamTemplateValueDto;
import com.ksptool.ql.biz.model.po.ModelVariantParamTemplatePo;
import com.ksptool.ql.biz.model.po.ModelVariantParamTemplateValuePo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamTemplateValueVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ksptool.entities.Entities.assign;

@Service
public class AdminModelVariantParamTemplateValueService {

    @Autowired
    private ModelVariantParamTemplateValueRepository repository;

    @Autowired
    private ModelVariantParamTemplateRepository templateRepository;

    /**
     * 查询指定模板的参数值列表（分页）
     * @param dto 查询参数
     * @return 模板值列表
     */
    public RestPageableView<GetModelVariantParamTemplateValueVo> getModelVariantParamTemplateValueList(GetModelVariantParamTemplateValueListDto dto) throws BizException, AuthException {
        Long currentUserId = AuthService.getCurrentUserId();

        // 1. 验证模板存在性及用户权限
        ModelVariantParamTemplatePo templateQuery = new ModelVariantParamTemplatePo();
        templateQuery.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        templateQuery.setUser(userQuery);

        ModelVariantParamTemplatePo template = templateRepository.findOne(Example.of(templateQuery))
                .orElseThrow(() -> new BizException("模板不存在或无权限访问"));

        // 2. 根据templateId、keyword和分页参数查询模板值列表
        Page<ModelVariantParamTemplateValuePo> page = repository.findByTemplateIdAndKeyword(template.getId(), dto.getKeyword(), dto.pageRequest());

        // 3. 将Po转换为Vo并返回
        List<GetModelVariantParamTemplateValueVo> vos = page.getContent().stream()
                .map(po -> {
                    GetModelVariantParamTemplateValueVo vo = new GetModelVariantParamTemplateValueVo();
                    assign(po, vo);
                    vo.setTemplateId(template.getId()); // 设置模板ID
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());

        return new RestPageableView<>(vos, page.getTotalElements());
    }

    /**
     * 查询模板参数值详情
     * @param dto 查询参数
     * @return 模板参数值详情
     */
    public GetModelVariantParamTemplateValueVo getModelVariantParamTemplateValueDetails(CommonIdDto dto) throws BizException, AuthException {
        Long currentUserId = AuthService.getCurrentUserId();

        ModelVariantParamTemplateValuePo templateValueQuery = new ModelVariantParamTemplateValuePo();
        templateValueQuery.setId(dto.getId());

        Optional<ModelVariantParamTemplateValuePo> optionalTemplateValue = repository.findOne(Example.of(templateValueQuery));

        ModelVariantParamTemplateValuePo templateValue = optionalTemplateValue
                .orElseThrow(() -> new BizException("模板参数值不存在"));

        // 验证用户权限：用户只能查看自己模板下的参数值
        ModelVariantParamTemplatePo template = templateValue.getTemplate();
        if (template == null) {
            throw new BizException("模板参数值关联的模板不存在");
        }
        if (!template.getUser().getId().equals(currentUserId)) {
            throw new BizException("无权限访问该模板参数值");
        }

        GetModelVariantParamTemplateValueVo vo = new GetModelVariantParamTemplateValueVo();
        assign(templateValue, vo);
        vo.setTemplateId(template.getId());

        return vo;
    }

    /**
     * 新增或编辑单个模板参数值
     * @param dto 保存参数
     */
    @Transactional
    public void saveModelVariantParamTemplateValue(SaveModelVariantParamTemplateValueDto dto) throws BizException, AuthException {
        Long currentUserId = AuthService.getCurrentUserId();

        ModelVariantParamTemplatePo templateQuery = new ModelVariantParamTemplatePo();
        templateQuery.setId(dto.getTemplateId());
        UserPo userQuery = new UserPo();
        userQuery.setId(currentUserId);
        templateQuery.setUser(userQuery);

        ModelVariantParamTemplatePo template = templateRepository.findOne(Example.of(templateQuery))
                .orElseThrow(() -> new BizException("模板不存在或无权限操作"));

        ModelVariantParamTemplateValuePo templateValue = null;
        if (dto.getId() != null) {
            // 编辑模式
            ModelVariantParamTemplateValuePo existingValueQuery = new ModelVariantParamTemplateValuePo();
            existingValueQuery.setId(dto.getId());
            existingValueQuery.setTemplate(template); // 确保是该模板下的值
            templateValue = repository.findOne(Example.of(existingValueQuery))
                    .orElseThrow(() -> new BizException("待编辑的模板参数值不存在或无权限操作"));

            // 检查paramKey唯一性（排除当前编辑的项）
            if (repository.findByTemplateIdAndParamKeyAndIdNot(dto.getTemplateId(), dto.getParamKey(), dto.getId()).isPresent()) {
                throw new BizException("参数键已存在");
            }
        }

        if (dto.getId() == null) {
            // 新增模式
            // 检查paramKey唯一性
            if (repository.findByTemplateIdAndParamKey(dto.getTemplateId(), dto.getParamKey()).isPresent()) {
                throw new BizException("参数键已存在");
            }
            templateValue = new ModelVariantParamTemplateValuePo();
            templateValue.setTemplate(template);
            // 自动管理seq：如果dto中未提供seq，则设置为当前模板下的最大seq + 1
            if (dto.getSeq() == null) {
                Long maxSeq = repository.findMaxSeqByTemplateId(dto.getTemplateId());
                if (maxSeq != null) {
                    templateValue.setSeq(maxSeq.intValue() + 1);
                }
                if (maxSeq == null) {
                    templateValue.setSeq(1);
                }
            }

            if (dto.getSeq() != null) {
                templateValue.setSeq(dto.getSeq());
            }
        }

        assign(dto, templateValue);
        repository.save(templateValue);
    }

    /**
     * 删除单个模板参数值
     * @param dto 删除参数
     */
    @Transactional
    public void removeModelVariantParamTemplateValue(CommonIdDto dto) throws BizException, AuthException {
        Long currentUserId = AuthService.getCurrentUserId();

        ModelVariantParamTemplateValuePo templateValueQuery = new ModelVariantParamTemplateValuePo();
        templateValueQuery.setId(dto.getId());

        Optional<ModelVariantParamTemplateValuePo> optionalTemplateValue = repository.findOne(Example.of(templateValueQuery));

        ModelVariantParamTemplateValuePo templateValue = optionalTemplateValue
                .orElseThrow(() -> new BizException("模板参数值不存在或无权限删除"));

        // 验证用户权限：用户只能删除自己模板下的参数值
        ModelVariantParamTemplatePo template = templateValue.getTemplate();
        if (template == null) {
            throw new BizException("模板参数值关联的模板不存在");
        }
        if (!template.getUser().getId().equals(currentUserId)) {
            throw new BizException("无权限删除该模板参数值");
        }

        repository.delete(templateValue);
    }
} 