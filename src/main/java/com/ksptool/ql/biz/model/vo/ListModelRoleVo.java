package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.web.PageableView;
import lombok.Data;
import java.util.Date;

/**
 * 模型角色列表视图对象
 * 用于模型角色管理页面的数据展示
 */
@Data
public class ListModelRoleVo {
    
    /**
     * 角色列表（带分页）
     */
    private PageableView<ListModelRoleItemVo> roleList;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 是否为新建角色模式
     */
    private Boolean isNew = false;
    
    // 以下是当前选中角色的详细信息，当id为null时表示未选中角色
    private Long id;

    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 头像路径
     */
    private String avatarPath;
    
    /**
     * 角色设定摘要
     */
    private String roleSummary;
    
    /**
     * 情景
     */
    private String scenario;
    
    /**
     * 首次对话内容
     */
    private String firstMessage;
    
    /**
     * 角色标签，多个标签用逗号分隔
     */
    private String tags;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 