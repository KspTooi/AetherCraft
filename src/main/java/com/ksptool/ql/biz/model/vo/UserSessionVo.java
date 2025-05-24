package com.ksptool.ql.biz.model.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;

import static com.ksptool.entities.Entities.assign;

@Data
public class UserSessionVo {
    private Long id;
    private Long userId;
    private Long playerId;
    private String playerName;
    private String playerAvatarUrl;
    private String token;
    private Set<String> permissions;
    private Date expiresAt;
    private Date createTime;
    private Date updateTime;

    public UserSessionVo(UserSessionPo po) {
        // 复制基本字段
        assign(po, this);
        
        // 反序列化权限
        try {
            this.permissions = new Gson().fromJson(
                po.getPermissions(),
                new TypeToken<HashSet<String>>(){}.getType()
            );
        } catch (Exception e) {
            // 如果解析失败，至少提供一个空集合
            this.permissions = new HashSet<>();
        }
    }

    public String getPlayerAvatarUrl(){
        if(StringUtils.isBlank(playerAvatarUrl)){
            return null;
        }
        return "/res/"+playerAvatarUrl;
    }

} 