package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PlayerDefaultGroupRepository;
import com.ksptool.ql.biz.model.dto.AddPlayerDefaultGroupDto;
import com.ksptool.ql.biz.model.dto.RemovePlayerDefaultGroupDto;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PlayerDefaultGroupPo;
import com.ksptool.ql.biz.model.vo.GetPlayerDefaultGroupListVo;
import com.ksptool.ql.commons.web.PageQuery;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminPlayerDefaultGroupService {

    @Autowired
    private PlayerDefaultGroupRepository repository;

    @Autowired
    private GroupRepository groupRepository;

    public RestPageableView<GetPlayerDefaultGroupListVo> getPlayerDefaultGroupList(PageQuery dto) {
        Page<GetPlayerDefaultGroupListVo> pageResult = repository.getPlayerDefaultGroupList(dto.pageRequest());
        return new RestPageableView<>(pageResult.getContent(), pageResult.getTotalElements());
    }

    @Transactional
    public void removePlayerDefaultGroup(RemovePlayerDefaultGroupDto dto) {
        List<Long> ids = dto.getIds();
        if (ids == null || ids.isEmpty()) {
            return; // 如果ID列表为空，则不执行任何操作
        }
        repository.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public void addPlayerDefaultGroup(AddPlayerDefaultGroupDto dto) {
        List<Long> incomingGroupIds = dto.getIds();
        if (incomingGroupIds == null || incomingGroupIds.isEmpty()) {
            return;
        }

        // 查询已存在的默认分组记录对应的 Group ID
        List<PlayerDefaultGroupPo> existingDefaults = repository.findByGroup_IdIn(incomingGroupIds);
        Set<Long> existingGroupIds = existingDefaults.stream()
                .map(pdg -> pdg.getGroup().getId())
                .collect(Collectors.toSet());

        // 筛选出需要新增的 Group ID
        List<Long> newGroupIds = incomingGroupIds.stream()
                .filter(id -> !existingGroupIds.contains(id))
                .collect(Collectors.toList());

        if (newGroupIds.isEmpty()) {
            return; // 没有需要新增的组
        }

        // 查询需要新增的 Group 实体
        List<GroupPo> groupsToAdd = groupRepository.findAllById(newGroupIds);
        List<PlayerDefaultGroupPo> defaultGroupsToSave = new ArrayList<>();

        for (GroupPo group : groupsToAdd) {
            PlayerDefaultGroupPo playerDefaultGroup = new PlayerDefaultGroupPo();
            playerDefaultGroup.setGroup(group);
            defaultGroupsToSave.add(playerDefaultGroup);
        }

        if (!defaultGroupsToSave.isEmpty()) {
            repository.saveAll(defaultGroupsToSave);
        }
    }

}
