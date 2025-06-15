package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.commons.web.RestPageableView;
import lombok.Data;

@Data
public class SelectThreadVo {

    private Long threadId;

    //模型变体ID
    private Long modelVariantId;

    private RestPageableView<SelectThreadMessageVo> messages;

}
