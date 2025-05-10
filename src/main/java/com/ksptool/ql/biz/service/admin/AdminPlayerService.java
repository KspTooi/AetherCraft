package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.model.dto.GetAdminPlayerListDto;
import com.ksptool.ql.biz.model.dto.EditAdminPlayerDto;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetAdminPlayerDetailsVo;
import com.ksptool.ql.biz.model.vo.GetAdminPlayerListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.ksptool.entities.Entities.assign;

@Service
public class AdminPlayerService {

    @Autowired
    private PlayerRepository repository;

    @Autowired
    private AuthService authService;
    @Autowired
    private GroupRepository groupRepository;

    public RestPageableView<GetAdminPlayerListVo> getPlayerList(GetAdminPlayerListDto dto) {

        Pageable pageQuery = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), Sort.Direction.DESC, "createTime");

        Page<GetAdminPlayerListVo> pVos = repository.getAdminPlayerList(
                dto.getPlayerName(),
                dto.getUsername(),
                dto.getStatus(),
                pageQuery
        );

        return new RestPageableView<>(pVos.getContent(), pVos.getTotalElements());
    }

    public GetAdminPlayerDetailsVo getPlayerDetails(Long id) throws BizException {
        PlayerPo playerPo = repository.findById(id)
                .orElseThrow(() -> new BizException("玩家不存在"));

        GetAdminPlayerDetailsVo vo = new GetAdminPlayerDetailsVo();
        assign(playerPo, vo); // 映射 PlayerPo 的同名属性

        // 设置用户名
        if (playerPo.getUser() != null) {
            vo.setUsername(playerPo.getUser().getUsername());
        }

        List<Long> groupIds = new ArrayList<>();

        //查询访问组
        playerPo.getGroups().forEach(group -> {
            groupIds.add(group.getId());
        });

        vo.setGroupIds(groupIds);
        return vo;
    }

    @Transactional
    public void editPlayer(EditAdminPlayerDto dto) throws BizException {

        // 编辑现有玩家
        PlayerPo playerPo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("要编辑的玩家不存在"));

        var oldStatus = playerPo.getStatus();
        var oldGender = playerPo.getGender();
        var oldGenderData = playerPo.getGenderData();

        // 先使用 assign 更新所有 DTO 中存在的字段
        assign(dto, playerPo);

        // 然后，如果 DTO 中传入了 status，则进行覆盖和校验
        if(dto.getStatus() != oldStatus){
            if (dto.getStatus() != null) {
                // 校验状态值是否为 1 或 3
                if (dto.getStatus() != 1 && dto.getStatus() != 3) {
                    throw new BizException("无效的状态值，只允许设置为 1 (不活跃) 或 3 (已删除)");
                }
                // 状态 0 (正在使用) 和 2 (等待删除) 不能通过此接口直接设置
                // (虽然 assign 可能已经设置了，但这里再次强制覆盖为 DTO 的值，并做最终校验)
                playerPo.setStatus(dto.getStatus()); // 强制使用 DTO 的值
            }
        }

        if (dto.getStatus() == null){
            playerPo.setStatus(oldStatus);
        }

        //前端传空则不更改性别
        if (dto.getGender() == null){
            playerPo.setGender(oldGender);
            playerPo.setGenderData(oldGenderData);
        }

        //处理访问组
        playerPo.setGroups(getGroupSet(dto.getGroupIds()));

        // 注意：不应通过此方法修改余额(balance)或所属用户(user)
        // 这些字段通常有独立的业务逻辑或管理接口
        repository.save(playerPo);

        // 如果玩家状态发生改变，刷新所属用户的会话信息
        authService.refreshUserSession(playerPo.getUser().getId());
    }

    private HashSet<GroupPo> getGroupSet(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(groupRepository.findAllById(groupIds));
    }
}
