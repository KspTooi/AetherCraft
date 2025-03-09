package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.commons.web.PageableView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.assign;

@Service
@RequiredArgsConstructor
public class ModelRpService {
    
    private final ModelRoleRepository modelRoleRepository;
    
    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto queryDto) {

        Page<ModelRolePo> page = modelRoleRepository.getModelRoleList(
            AuthService.getCurrentUserId(),
            queryDto.getKeyword(),
            queryDto.pageRequest()
        );

        return new PageableView<>(page,GetModelRoleListVo.class);
    }
} 