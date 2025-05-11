package com.ksptool.ql.biz.service.admin;

import com.google.gson.Gson;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.UserSessionRepository;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.ksptool.ql.biz.model.dto.GetSessionListDto;
import com.ksptool.ql.biz.model.vo.GetSessionListVo;
import com.ksptool.ql.biz.model.vo.GetSessionDetailsVo;
import com.ksptool.ql.commons.web.RestPageableView;

import java.util.List;
import java.util.Set;

@Service
public class AdminSessionService {

    @Autowired
    private UserSessionRepository repository;

    @Autowired
    private UserRepository userRepository;


    public RestPageableView<GetSessionListVo> getSessionList(GetSessionListDto dto) {
        return new RestPageableView<>(repository.getSessionList(dto, dto.pageRequest()));
    }

    public GetSessionDetailsVo getSessionDetails(Long id) throws BizException {

        UserSessionPo session = repository.findById(id)
                .orElseThrow(() -> new BizException("会话不存在"));

        GetSessionDetailsVo vo = new GetSessionDetailsVo();
        vo.setId(session.getId());
        vo.setUsername("----");
        userRepository.findById(session.getUserId())
                .ifPresent(user -> {vo.setUsername(user.getUsername());});
        vo.setPlayerName(session.getPlayerName());
        vo.setCreateTime(session.getCreateTime());
        vo.setExpiresAt(session.getExpiresAt());

        Gson gson = new Gson();
        vo.setPermissions(gson.fromJson(session.getPermissions(), Set.class));
        return vo;
    }

    public void closeSession(Long id) throws BizException {

        UserSessionPo session = repository.findById(id)
                .orElseThrow(() -> new BizException("会话不存在"));

        repository.delete(session);
    }
}
