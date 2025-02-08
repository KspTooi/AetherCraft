package com.ksptool.ql.biz.model.vo;

import com.ksptool.ql.biz.model.po.AppPo;
import lombok.Data;

import java.util.List;

@Data
public class AppCenterVo {

    private int appCount;

    private List<AppItemVo> appList;

}
