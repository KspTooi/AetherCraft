package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.AppPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface AppRepository extends JpaRepository<AppPo, Long> {

    @Modifying
    @Transactional
    int deleteByIdAndUserId(Long id, Long userId);

    Optional<AppPo> findByIdAndUserId(Long id, Long userId);
}