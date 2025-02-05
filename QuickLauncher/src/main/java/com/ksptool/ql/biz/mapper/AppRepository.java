package com.ksptool.ql.biz.mapper;


import com.ksptool.ql.biz.model.po.AppPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends JpaRepository<AppPo, Long> {

}