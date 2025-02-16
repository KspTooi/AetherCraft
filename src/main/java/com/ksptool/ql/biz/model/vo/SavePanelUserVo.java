package com.ksptool.ql.biz.model.vo;

import java.util.List;
import lombok.Data;

// 用户保存视图
@Data
public class SavePanelUserVo {
    
    // 用户ID
    private Long id;
    
    // 用户名
    private String username;
    
    // 昵称
    private String nickname;
    
    // 邮箱
    private String email;
    
    // 用户组列表
    private List<SavePanelUserGroupVo> groups;
} 