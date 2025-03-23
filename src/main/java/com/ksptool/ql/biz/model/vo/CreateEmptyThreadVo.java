package com.ksptool.ql.biz.model.vo;

import lombok.Data;

@Data
public class CreateEmptyThreadVo {
    
    // 新创建的会话ID
    private Long threadId;
    
    public static CreateEmptyThreadVo of(Long threadId) {
        CreateEmptyThreadVo vo = new CreateEmptyThreadVo();
        vo.setThreadId(threadId);
        return vo;
    }
}