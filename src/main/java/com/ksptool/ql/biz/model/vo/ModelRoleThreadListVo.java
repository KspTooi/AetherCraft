package com.ksptool.ql.biz.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ModelRoleThreadListVo {

    // 会话线程ID
    private Long id;
    
    // 会话标题
    private String title;

    // 会话描述
    private String description;
    
    // AI模型代码
    private String modelCode;
    
    // 是否为当前激活的对话 0-存档 1-激活
    private Integer active;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
    // 最后一条消息内容（可选，如果需要显示预览）
    private String lastMessage;
} 