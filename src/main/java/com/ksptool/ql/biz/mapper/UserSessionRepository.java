package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.dto.GetSessionListDto;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import com.ksptool.ql.biz.model.vo.GetSessionListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionPo, Long> {


    @Query("""
          SELECT new com.ksptool.ql.biz.model.vo.GetSessionListVo(
            us.id,
            u.username,
            us.playerName,
            us.createTime,
            us.expiresAt
          ) FROM UserSessionPo us
          LEFT JOIN UserPo u ON us.userId = u.id
          WHERE (:#{#dto.userName} IS NULL OR u.username LIKE %:#{#dto.userName}%)
          AND (:#{#dto.playerName} IS NULL OR us.playerName LIKE %:#{#dto.playerName}%)
          ORDER BY us.createTime DESC
          """)
    Page<GetSessionListVo> getSessionList(@Param("dto") GetSessionListDto dto, Pageable page);

    UserSessionPo findByToken(String token);

    UserSessionPo findByUserId(Long userId);

    void deleteByToken(String token);
    
    /**
     * 根据用户组ID查询该组下所有在线用户的会话信息
     * 在线用户的判断标准是会话未过期（expiresAt > 当前时间）
     * 
     * @param groupId 用户组ID
     * @return 在线用户的会话信息列表
     */
    @Query("""
          SELECT DISTINCT us FROM UserSessionPo us
          LEFT JOIN UserPo u ON us.userId = u.id
          LEFT JOIN u.groups ug
          LEFT JOIN PlayerPo pl ON us.playerId = pl.id
          LEFT JOIN pl.groups pg
          WHERE (ug.id = :groupId OR pg.id = :groupId)
          AND us.expiresAt > CURRENT_TIMESTAMP
          ORDER BY us.userId
          """)
    List<UserSessionPo> getUserSessionByGroupId(@Param("groupId") Long groupId);



}