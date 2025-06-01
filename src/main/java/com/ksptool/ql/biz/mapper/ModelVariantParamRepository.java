package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantParamPo;
import com.ksptool.ql.biz.model.vo.GetModelVariantParamListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelVariantParamRepository extends JpaRepository<ModelVariantParamPo, Long> {

    /**
     * 查询模型变体参数列表，返回全局参数和用户参数供Service层合并
     */
    @Query("""
            SELECT p FROM ModelVariantParamPo p
            WHERE p.modelVariant.id = :modelVariantId
            AND (:keyword IS NULL OR :keyword = '' OR 
                 p.paramKey LIKE CONCAT('%', :keyword, '%') OR 
                 p.description LIKE CONCAT('%', :keyword, '%'))
            AND (p.user IS NULL AND p.player IS NULL OR 
                 (p.user.id = :currentUserId AND p.player.id = :currentPlayerId))
            ORDER BY p.seq ASC, p.createTime DESC
            """)
    List<ModelVariantParamPo> getModelVariantParamListForMerge(
            @Param("modelVariantId") Long modelVariantId,
            @Param("keyword") String keyword,
            @Param("currentUserId") Long currentUserId,
            @Param("currentPlayerId") Long currentPlayerId
    );

    /**
     * 根据模型变体ID和参数键查询全局参数
     */
    @Query("""
            SELECT p FROM ModelVariantParamPo p
            WHERE p.modelVariant.id = :modelVariantId
            AND p.paramKey = :paramKey
            AND p.user IS NULL AND p.player IS NULL
            """)
    ModelVariantParamPo findByModelVariantIdAndParamKeyAndUserIsNullAndPlayerIsNull(
            @Param("modelVariantId") Long modelVariantId,
            @Param("paramKey") String paramKey
    );

    /**
     * 根据模型变体ID、参数键、用户ID和玩家ID查询用户参数
     */
    @Query("""
            SELECT p FROM ModelVariantParamPo p
            WHERE p.modelVariant.id = :modelVariantId
            AND p.paramKey = :paramKey
            AND p.user.id = :userId
            AND p.player.id = :playerId
            """)
    ModelVariantParamPo findByModelVariantIdAndParamKeyAndUserIdAndPlayerId(
            @Param("modelVariantId") Long modelVariantId,
            @Param("paramKey") String paramKey,
            @Param("userId") Long userId,
            @Param("playerId") Long playerId
    );

} 