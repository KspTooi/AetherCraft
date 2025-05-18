package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.web.RestPageableView;
import lombok.Data;

@Data
public class SelectThreadVo {

    private Long threadId;

    private String modelCode;

    private RestPageableView<SelectThreadMessageVo> messages;

}
