package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserPo, Long> {

    /**
     * 根据用户名查找用户
     */
    UserPo findByUsername(String username);

}