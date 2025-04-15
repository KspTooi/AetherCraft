package com.ksptool.ql.biz.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class SaveUserDto {
    /**
     * 用户ID，创建时为空
     */
    private Long id;
    
    /**
     * 用户名
     */
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含4-20位字母、数字和下划线")
    private String username;
    
    /**
     * 用户密码，创建时必填，编辑时可选
     */
    private String password;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 用户状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 用户组ID列表
     */
    private List<Long> groupIds;
}
