package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserSessionPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionPo, Long> {

    UserSessionPo findByToken(String token);

    UserSessionPo findByUserId(Long userId);

    void deleteByToken(String token);
}