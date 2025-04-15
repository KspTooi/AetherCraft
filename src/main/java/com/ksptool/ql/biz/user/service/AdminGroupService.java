package com.ksptool.ql.biz.user.service;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.user.model.dto.GetGroupListDto;
import com.ksptool.ql.biz.user.model.dto.SaveGroupDto;
import com.ksptool.ql.biz.user.model.vo.GetGroupDetailsVo;
import com.ksptool.ql.biz.user.model.vo.GetGroupListVo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.RestPageableView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminGroupService {

    @Autowired
    private GroupRepository repository;

    public RestPageableView<GetGroupListVo> getGroupList(GetGroupListDto dto) {
        return null;
    }

    public GetGroupDetailsVo getGroupDetails(long id) throws BizException {
        return null;
    }

    public void saveGroup(SaveGroupDto dto) throws BizException {
        
    }

    public void removeGroup(long id) throws BizException {
        
    }
}
