package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserThemeValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户主题值数据访问接口
 */
@Repository
public interface UserThemeValuesRepository extends JpaRepository<UserThemeValues, Long> {
    
    /**
     * 根据主题ID和键名查找主题值
     */
    UserThemeValues findByTheme_IdAndThemeKey(Long themeId, String themeKey);
    
    /**
     * 根据主题ID查找所有主题值
     */
    List<UserThemeValues> findByTheme_Id(Long themeId);
    
    /**
     * 删除指定主题的所有值
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM UserThemeValues v 
            WHERE v.theme.id = :themeId
            """)
    void deleteByThemeId(@Param("themeId") Long themeId);
} 