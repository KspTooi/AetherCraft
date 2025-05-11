package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PlayerThemePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户主题数据访问接口
 */
@Repository
public interface PlayerThemeRepository extends JpaRepository<PlayerThemePo, Long> {
    
    /**
     * 查询玩家的所有主题，带主题值
     */
    @Query("""
            SELECT t FROM PlayerThemePo t
            LEFT JOIN FETCH t.themeValues
            WHERE t.player.id = :playerId
            """)
    List<PlayerThemePo> findByPlayerIdWithValues(@Param("playerId") Long playerId);
    
    /**
     * 获取玩家的激活主题
     */
    @Query("""
            SELECT t FROM PlayerThemePo t
            LEFT JOIN FETCH t.themeValues
            WHERE t.player.id = :playerId AND t.isActive = 1
            """)
    PlayerThemePo findActiveThemeByPlayerId(@Param("playerId") Long playerId);

} 