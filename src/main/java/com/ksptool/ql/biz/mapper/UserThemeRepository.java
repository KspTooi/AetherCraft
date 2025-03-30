package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.UserTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户主题数据访问接口
 */
@Repository
public interface UserThemeRepository extends JpaRepository<UserTheme, Long> {
    
    /**
     * 查询用户的所有主题，带主题值
     */
    @Query("""
            SELECT t FROM UserTheme t
            LEFT JOIN FETCH t.themeValues
            WHERE t.userId = :userId
            ORDER BY t.isActive DESC, t.updateTime DESC
            """)
    List<UserTheme> findByUserIdWithValues(@Param("userId") Long userId);
    
    /**
     * 获取用户的激活主题
     */
    @Query("""
            SELECT t FROM UserTheme t
            LEFT JOIN FETCH t.themeValues
            WHERE t.userId = :userId AND t.isActive = 1
            """)
    UserTheme findActiveThemeByUserId(@Param("userId") Long userId);
    
    /**
     * 判断主题名称是否存在
     */
    boolean existsByUserIdAndThemeName(Long userId, String themeName);
} 