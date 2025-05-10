package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetAdminPlayerListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerPo, Long>, JpaSpecificationExecutor<PlayerPo> {

    boolean existsByName(String name);

    @Query("""
           SELECT p FROM PlayerPo p
           WHERE p.user.id = :userId AND p.status != 3
           AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.publicInfo) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<PlayerPo> getPlayerList(@Param("keyword")String keyword,
                                 @Param("userId")Long userId,
                                 Pageable page);

    @Modifying
    @Query("""
           UPDATE PlayerPo p
           SET p.status = 1
           WHERE p.user.id = :userId AND p.status = 0
           """)
    void detachAllActivePlayersByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT new com.ksptool.ql.biz.model.vo.GetAdminPlayerListVo(
                p.id,
                p.name,
                p.user.username,
                p.balance,
                p.status,
                p.createTime,
                SIZE(p.groups)
            )
            FROM PlayerPo p JOIN p.user u
            WHERE (:playerName IS NULL OR p.name LIKE CONCAT('%', :playerName, '%'))
            AND (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
            AND (:status IS NULL OR p.status = :status)
            ORDER BY p.createTime DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM PlayerPo p JOIN p.user u
            WHERE (:playerName IS NULL OR p.name LIKE CONCAT('%', :playerName, '%'))
            AND (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
            AND (:status IS NULL OR p.status = :status)
            """)
    Page<GetAdminPlayerListVo> getAdminPlayerList(
            @Param("playerName") String playerName,
            @Param("username") String username,
            @Param("status") Integer status,
            Pageable pageable
    );

    // 获取人物的所有权限
    @Query("""
            SELECT DISTINCT p
            FROM PlayerPo pl
            JOIN pl.groups g
            JOIN g.permissions p
            WHERE pl.id = :playerId
            """)
    List<PermissionPo> getPermissionByPlayerId(@Param("playerId") Long playerId);

    List<PlayerPo> findByName(String name);

    PlayerPo findOneByName(String name);
}
