package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserPo, Long> {



    UserPo findByUsername(String username);
}