package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.PlayerPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerPo, Long> {
    boolean existsByName(String name);

    @Query("""
           SELECT p FROM PlayerPo p
           WHERE p.user.id = :userId
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

}
