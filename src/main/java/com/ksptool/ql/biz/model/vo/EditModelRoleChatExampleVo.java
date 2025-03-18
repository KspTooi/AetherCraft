package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class EditModelRoleChatExampleVo {
    //示例ID
    private Long id;
    
    //关联的角色ID
    private Long modelRoleId;
    
    //对话内容
    private String content;
    
    //排序号
    private Integer sortOrder;
}
