package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.vo.ListPanelAppVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface AppRepository extends JpaRepository<AppPo, Long> {

    @Modifying
    @Transactional
    int deleteByIdAndUserId(Long id, Long userId);

    Optional<AppPo> findByIdAndUserId(Long id, Long userId);

    boolean existsByCommand(String command);

    @Query("""
            SELECT new com.ksptool.ql.biz.model.vo.ListPanelAppVo(
                a.id, a.name, a.kind, a.execPath, a.iconPath, a.command, 
                a.description, a.launchCount, a.lastLaunchTime, a.createTime, a.updateTime
            )
            FROM AppPo a
            WHERE (:nameOrCommand IS NULL OR a.name LIKE %:nameOrCommand% OR a.command LIKE %:nameOrCommand%)
            AND (:description IS NULL OR a.description LIKE %:description%)
            """)
    Page<ListPanelAppVo> getListView(@Param("nameOrCommand") String nameOrCommand, 
                                    @Param("description") String description, 
                                    Pageable pageable);
}