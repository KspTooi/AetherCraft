package com.ksptool.ql.biz.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavePanelUserDto {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码
     */
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间", groups = {OnPasswordNotEmpty.class})
    private String password;
    
    /**
     * 密码非空时的验证组
     */
    public interface OnPasswordNotEmpty {}
} 