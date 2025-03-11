package com.ksptool.ql.biz.mapper;

import com.ksptool.ql.biz.model.po.ModelRpSegmentPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRpSegmentRepository extends JpaRepository<ModelRpSegmentPo, Long> {

    /**
     * 删除指定会话的所有片段
     * @param threadId 会话ID
     */
    void deleteByThreadId(Long threadId);

    /**
     * 查找指定会话的最大序号
     * @param threadId 会话ID
     * @return 最大序号
     */
    @Query("SELECT COALESCE(MAX(s.sequence), 0) FROM ModelRpSegmentPo s WHERE s.thread.id = ?1")
    int findMaxSequenceByThreadId(Long threadId);

    /**
     * 查找指定会话的下一个未读片段
     * @param threadId 会话ID
     * @return 未读片段列表
     */
    @Query("SELECT s FROM ModelRpSegmentPo s WHERE s.thread.id = ?1 AND s.status = 0 ORDER BY s.sequence ASC")
    List<ModelRpSegmentPo> findNextUnreadByThreadId(Long threadId);
} 