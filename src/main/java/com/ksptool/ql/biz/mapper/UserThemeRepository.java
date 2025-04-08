package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserThemePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户主题数据访问接口
 */
@Repository
public interface UserThemeRepository extends JpaRepository<UserThemePo, Long> {
    
    /**
     * 查询用户的所有主题，带主题值
     */
    @Query("""
            SELECT t FROM UserThemePo t
            LEFT JOIN FETCH t.themeValues
            WHERE t.user.id = :userId
            """)
    List<UserThemePo> findByUserIdWithValues(@Param("userId") Long userId);
    
    /**
     * 获取用户的激活主题
     */
    @Query("""
            SELECT t FROM UserThemePo t
            LEFT JOIN FETCH t.themeValues
            WHERE t.user.id = :userId AND t.isActive = 1
            """)
    UserThemePo findActiveThemeByUserId(@Param("userId") Long userId);
    
    /**
     * 判断主题名称是否存在
     */
    boolean existsByUser_IdAndThemeName(Long userId, String themeName);
} 