package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelVariantParamPo;
import org.springframework.data.jpa.repository.JpaRepository;
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
            ORDER BY p.seq ASC
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

    /**
     * 查询指定模型变体的所有全局参数
     */
    @Query("""
            SELECT p FROM ModelVariantParamPo p
            WHERE p.modelVariant.id = :modelVariantId
            AND p.user IS NULL AND p.player IS NULL
            """)
    List<ModelVariantParamPo> getGlobalParamList(
            @Param("modelVariantId") Long modelVariantId
    );

    /**
     * 查询指定模型变体下指定用户的所有个人参数
     */
    @Query("""
            SELECT p FROM ModelVariantParamPo p
            WHERE p.modelVariant.id = :modelVariantId
            AND p.user.id = :userId
            AND p.player.id = :playerId
            """)
    List<ModelVariantParamPo> findByModelVariantIdAndUserIdAndPlayerId(
            @Param("modelVariantId") Long modelVariantId,
            @Param("userId") Long userId,
            @Param("playerId") Long playerId
    );

    /**
     * 查询所有模型变体参数的最大排序号
     * @return 最大排序号，如果不存在则返回0
     */
    @Query("""
            SELECT COALESCE(MAX(p.seq), 0) FROM ModelVariantParamPo p
            """)
    int getMaxSeq();

} 