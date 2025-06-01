package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelVariantRepository;
import com.ksptool.ql.biz.model.dto.GetAdminModelVariantListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelVariantDto;
import com.ksptool.ql.biz.model.po.ModelVariantPo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelVariantListVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

import static com.ksptool.entities.Entities.assign;

@Service
public class ModelVariantService {

    @Autowired
    private ModelVariantRepository repository;

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
                Integer maxSeq = repository.findAll().stream()
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

}
