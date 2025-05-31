package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelSeriesRepository;
import com.ksptool.ql.biz.model.dto.GetAdminModelSeriesListDto;
import com.ksptool.ql.biz.model.dto.SaveAdminModelSeriesDto;
import com.ksptool.ql.biz.model.po.ModelSeriesPo;
import com.ksptool.ql.biz.model.vo.GetAdminModelSeriesDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminModelSeriesListVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import static com.ksptool.entities.Entities.assign;

@Service
public class ModelSeriesService {

    @Autowired
    private ModelSeriesRepository repository;

    public RestPageableView<GetAdminModelSeriesListVo> getModelSeriesList(GetAdminModelSeriesListDto dto) {
        Page<ModelSeriesPo> pageResult = repository.getAdminModelSeriesList(
                dto.getKeyword(),
                dto.getEnabled(),
                dto.pageRequest()
        );

        List<GetAdminModelSeriesListVo> vos = new ArrayList<>();
        for (ModelSeriesPo po : pageResult.getContent()) {
            GetAdminModelSeriesListVo vo = new GetAdminModelSeriesListVo();
            assign(po, vo);
            vos.add(vo);
        }

        return new RestPageableView<>(vos, pageResult.getTotalElements());
    }

    public GetAdminModelSeriesDetailsVo getModelSeriesDetails(Long id) throws BizException {
        ModelSeriesPo po = repository.findById(id)
                .orElseThrow(() -> new BizException("模型系列不存在"));

        GetAdminModelSeriesDetailsVo vo = new GetAdminModelSeriesDetailsVo();
        assign(po, vo);
        return vo;
    }

    @Transactional
    public void saveModelSeries(SaveAdminModelSeriesDto dto) throws BizException {
        if (dto.getId() == null) {
            // 新增
            if (repository.existsByCode(dto.getCode())) {
                throw new BizException("模型代码已存在");
            }

            ModelSeriesPo po = new ModelSeriesPo();
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
        ModelSeriesPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("要编辑的模型系列不存在"));

        if (repository.existsByCodeAndIdNot(dto.getCode(), dto.getId())) {
            throw new BizException("模型代码已存在");
        }

        assign(dto, po);
        repository.save(po);
    }

    @Transactional
    public void removeModelSeries(Long id) throws BizException {
        ModelSeriesPo po = repository.findById(id)
                .orElseThrow(() -> new BizException("要删除的模型系列不存在"));

        repository.delete(po);
    }

}
