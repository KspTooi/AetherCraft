package com.ksptool.ql.biz.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class GetThreadListVo {

    //ThreadId
    private Long id;

    //(明文)会话标题
    private String title;

    //最后一条消息预览
    private String lastMessage;

    //(明文)会话公开信息
    private String publicInfo;

    //模型代码
    private String modelCode;

    //是否为当前激活的对话 0:缓解 1:激活
    private Integer active;

    //创建时间
    private Date createTime;

    //更新时间
    private Date updateTime;

}
