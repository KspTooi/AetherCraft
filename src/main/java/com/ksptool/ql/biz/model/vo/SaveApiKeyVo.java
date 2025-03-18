package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * API密钥保存/编辑视图数据
 */
@Data
public class SaveApiKeyVo {
    
    /**
     * 密钥ID（新增时为null）
     */
    private Long id;
    
    /**
     * 密钥名称
     */
    private String keyName;
    
    /**
     * 密钥系列
     */
    private String keySeries;
    
    /**
     * 密钥值
     */
    private String keyValue;
    
    /**
     * 是否共享：0-不共享，1-共享
     */
    private Integer isShared;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 使用次数（只读）
     */
    private Long usageCount;
    
    /**
     * 最后使用时间（只读）
     */
    private Date lastUsedTime;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    // 系列列表，用于前端选择
    private List<String> keySeriesList;
} 