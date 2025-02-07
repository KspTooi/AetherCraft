package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.AppInstancePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 应用实例数据访问接口
 */
public interface AppInstanceRepository extends JpaRepository<AppInstancePo, Long> {

    /**
     * 根据用户ID和运行状态查询应用实例列表
     * @param userId 用户ID
     * @param status 运行状态（0:运行中, 1:已终止）
     * @return 应用实例列表
     */
    List<AppInstancePo> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据用户ID和应用ID查询运行中的实例
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 运行中的实例列表
     */
    List<AppInstancePo> findByUserIdAndAppIdAndStatus(Long userId, Long appId, Integer status);

    /**
     * 根据PID查询应用实例
     * @param pid 进程ID
     * @return 应用实例（可能不存在）
     */
    Optional<AppInstancePo> findByPid(Long pid);

    /**
     * 查询用户的运行中实例数量
     * @param userId 用户ID
     * @return 运行中的实例数量
     */
    @Query("SELECT COUNT(ai) FROM AppInstancePo ai WHERE ai.userId = :userId AND ai.status = 0")
    long countRunningInstances(@Param("userId") Long userId);
} 