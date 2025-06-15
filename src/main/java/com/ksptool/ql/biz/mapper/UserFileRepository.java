package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserFilePo;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileRepository extends JpaRepository<UserFilePo, Long> {
    UserFilePo findByFilepathAndUserId(String filepath, Long userId);
} 