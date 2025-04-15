package com.ksptool.ql.biz.user.service;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.user.model.dto.GetGroupListDto;
import com.ksptool.ql.biz.user.model.dto.SaveGroupDto;
import com.ksptool.ql.biz.user.model.vo.GetGroupDefinitionsVo;
import com.ksptool.ql.biz.user.model.vo.GetGroupDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetGroupListVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.RestPageableView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ksptool.entities.Entities.as;

@Service
public class AdminGroupService {

    @Autowired
    private GroupRepository repository;


    public List<GetGroupDefinitionsVo> getGroupDefinitions(){
        List<GroupPo> pos = repository.findAll();
        return as(pos,GetGroupDefinitionsVo.class);
    }

    public RestPageableView<GetGroupListVo> getGroupList(GetGroupListDto dto) {
        Page<GetGroupListVo> pagePos = repository.getGroupList(dto, dto.pageRequest());
        return new RestPageableView<>(pagePos);
    }

    public GetGroupDetailsVo getGroupDetails(long id) throws BizException {
        return null;
    }

    public void saveGroup(SaveGroupDto dto) throws BizException {
        
    }

    public void removeGroup(long id) throws BizException {
        
    }
}
