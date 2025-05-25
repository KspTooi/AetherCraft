package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class GetNpcDetailsVo {

    // 角色ID
    private Long id;

    // 角色名称
    private String name;

    // 头像路径
    private String avatarPath;

    // 角色描述
    private String description;

    // 角色设定摘要
    private String roleSummary;

    // 情景
    private String scenario;

    // 首次对话内容
    private String firstMessage;

    // 角色标签，多个标签用逗号分隔
    private String tags;

    // 排序号
    private Integer sortOrder;

    // 状态：0-禁用，1-启用
    private Integer status;

}
