package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

// 用户保存数据传输对象
@Data
public class SavePanelUserDto {
    
    // 用户ID
    private Long id;
    
    // 用户名
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    // 昵称
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    // 邮箱
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    // 密码
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间", groups = {OnPasswordNotEmpty.class})
    private String password;
    
    // 用户组ID列表
    private List<Long> groupIds;
    
    /**
     * 密码非空时的验证组
     */
    public interface OnPasswordNotEmpty {}
} 