package com.ksptool.ql.biz.service.admin;

import com.ksptool.ql.biz.mapper.ModelVariantParamRepository;
import com.ksptool.ql.biz.mapper.ModelVariantRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamListDto;
import com.ksptool.ql.biz.model.dto.GetModelVariantParamDetailsDto;
import com.ksptool.ql.biz.model.dto.RemoveModelVariantParamDto;
import com.ksptool.ql.biz.model.dto.SaveModelVariantParamDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamDetailsVo;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.AuthException;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.assign;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminModelVariantParamService {

    @Autowired
    private ModelVariantParamRepository repository;

    @Autowired
    private ModelVariantRepository modelVariantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * 查询模型变体参数列表，包含全局值和用户值
     * @param dto 查询参数
     * @return 参数列表
     */
    public RestPageableView<GetModelVariantParamListVo> getModelVariantParamList(GetModelVariantParamListDto dto) throws AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();
        Long currentPlayerId = AuthService.requirePlayerId();

        // 获取所有相关参数
        List<ModelVariantParamPo> allParams = repository.getModelVariantParamListForMerge(
                dto.getModelVariantId(),
                dto.getKeyword(),
                currentUserId,
                currentPlayerId
        );

        // 按参数键分组合并
        Map<String, GetModelVariantParamListVo> mergedMap = new LinkedHashMap<>();
        
        for (ModelVariantParamPo param : allParams) {
            String paramKey = param.getParamKey();
            GetModelVariantParamListVo vo = mergedMap.get(paramKey);
            
            if (vo == null) {
                vo = new GetModelVariantParamListVo();
                vo.setParamKey(paramKey);
                vo.setType(param.getType());
                vo.setDescription(param.getDescription());
                vo.setSeq(param.getSeq());
                vo.setCreateTime(param.getCreateTime());
                vo.setUpdateTime(param.getUpdateTime());
                mergedMap.put(paramKey, vo);
            }
            
            // 判断是全局参数还是用户参数
            if (param.getUser() == null && param.getPlayer() == null) {
                // 全局参数
                vo.setGlobalVal(param.getParamVal());
            }
            if (param.getUser() != null && param.getPlayer() != null) {
                // 用户参数
                vo.setUserVal(param.getParamVal());
            }
        }
        
        // 转换为列表并分页
        List<GetModelVariantParamListVo> resultList = new ArrayList<>(mergedMap.values());
        
        // 手动分页
        int pageNumber = dto.getPage() - 1; // PageQuery从1开始，转换为从0开始
        int pageSize = dto.getPageSize();
        int totalElements = resultList.size();
        
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, totalElements);
        
        List<GetModelVariantParamListVo> pageContent = new ArrayList<>();
        if (start < totalElements) {
            pageContent = resultList.subList(start, end);
        }
        
        // 创建分页对象
        return new RestPageableView<GetModelVariantParamListVo>(pageContent, (long) totalElements);
    }

    /**
     * 查询参数详情
     * @param dto 查询参数
     * @return 参数详情
     */
    public GetModelVariantParamDetailsVo getModelVariantParamDetails(GetModelVariantParamDetailsDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();
        Long currentPlayerId = AuthService.requirePlayerId();

        ModelVariantParamPo param = null;
        
        if (dto.getGlobal() == 1) {
            // 查询全局参数
            param = repository.findByModelVariantIdAndParamKeyAndUserIsNullAndPlayerIsNull(
                    dto.getModelVariantId(), dto.getParamKey());
        }
        if (dto.getGlobal() == 0) {
            // 查询用户参数
            param = repository.findByModelVariantIdAndParamKeyAndUserIdAndPlayerId(
                    dto.getModelVariantId(), dto.getParamKey(), currentUserId, currentPlayerId);
        }

        if (param == null) {
            throw new BizException("参数配置不存在");
        }

        GetModelVariantParamDetailsVo vo = new GetModelVariantParamDetailsVo();
        assign(param, vo);
        
        // 设置global标识
        vo.setGlobal(dto.getGlobal());
        
        // 设置关联实体的ID
        if (param.getModelVariant() != null) {
            vo.setModelVariantId(param.getModelVariant().getId());
        }
        if (param.getUser() != null) {
            vo.setUserId(param.getUser().getId());
        }
        if (param.getPlayer() != null) {
            vo.setPlayerId(param.getPlayer().getId());
        }

        return vo;
    }

    /**
     * 保存参数配置，可保存全局或个人参数
     * @param dto 保存参数
     */
    @Transactional
    public void saveModelVariantParam(SaveModelVariantParamDto dto) throws BizException, AuthException {
        // 验证模型变体是否存在
        ModelVariantPo modelVariant = modelVariantRepository.findById(dto.getModelVariantId())
                .orElseThrow(() -> new BizException("模型变体不存在"));

        // 设置用户和玩家信息
        UserPo user = null;
        PlayerPo player = null;
        
        if (dto.getGlobal() == 0) {
            // 个人参数，设置当前用户和玩家
            Long currentUserId = AuthService.getCurrentUserId();
            Long currentPlayerId = AuthService.requirePlayerId();
            
            if (currentUserId == null || currentPlayerId == null) {
                throw new BizException("用户信息获取失败");
            }
            
            user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BizException("用户不存在"));
            player = playerRepository.findById(currentPlayerId)
                    .orElseThrow(() -> new BizException("玩家不存在"));
        }

        // 通过三要素查找现有参数
        ModelVariantParamPo existingParam = null;
        if (dto.getGlobal() == 1) {
            // 查找全局参数
            existingParam = repository.findByModelVariantIdAndParamKeyAndUserIsNullAndPlayerIsNull(
                    dto.getModelVariantId(), dto.getParamKey());
        }
        if (dto.getGlobal() == 0) {
            // 查找用户参数
            Long currentUserId = AuthService.getCurrentUserId();
            Long currentPlayerId = AuthService.requirePlayerId();
            existingParam = repository.findByModelVariantIdAndParamKeyAndUserIdAndPlayerId(
                    dto.getModelVariantId(), dto.getParamKey(), currentUserId, currentPlayerId);
        }

        ModelVariantParamPo param = null;
        if (existingParam != null) {
            // 编辑现有参数
            param = existingParam;
        } else {
            // 新增参数
            param = new ModelVariantParamPo();
            param.setModelVariant(modelVariant);
            param.setParamKey(dto.getParamKey());
            param.setUser(user);
            param.setPlayer(player);
        }

        // 设置基础信息
        param.setParamVal(dto.getParamVal());
        param.setType(dto.getType());
        param.setDescription(dto.getDescription());

        // 设置排序号
        if (dto.getSeq() != null) {
            param.setSeq(dto.getSeq());
        }
        if (dto.getSeq() == null && existingParam == null) {
            // 新增时，如果未提供排序号，自动设置为最大值+1
            Integer maxSeq = repository.findAll().stream()
                    .mapToInt(p -> p.getSeq() != null ? p.getSeq() : 0)
                    .max()
                    .orElse(0);
            param.setSeq(maxSeq + 1);
        }

        repository.save(param);
    }

    /**
     * 删除参数配置
     * @param dto 删除参数
     */
    @Transactional
    public void removeModelVariantParam(RemoveModelVariantParamDto dto) throws BizException, AuthException {
        // 获取当前用户信息
        Long currentUserId = AuthService.getCurrentUserId();
        Long currentPlayerId = AuthService.requirePlayerId();

        ModelVariantParamPo param = null;
        
        if (dto.getGlobal() == 1) {
            // 删除全局参数
            param = repository.findByModelVariantIdAndParamKeyAndUserIsNullAndPlayerIsNull(
                    dto.getModelVariantId(), dto.getParamKey());
        }
        if (dto.getGlobal() == 0) {
            // 删除用户参数
            param = repository.findByModelVariantIdAndParamKeyAndUserIdAndPlayerId(
                    dto.getModelVariantId(), dto.getParamKey(), currentUserId, currentPlayerId);
        }

        if (param == null) {
            throw new BizException("参数配置不存在");
        }

        repository.delete(param);
    }

} 