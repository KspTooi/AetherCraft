package com.ksptool.ql.restcgi.model;

import com.ksptool.ql.commons.enums.AIModelEnum;
import lombok.Data;

@Data
public class CgiChatResult {

    //模型枚举
    private AIModelEnum model;

    //返回类型 0:数据 1:结束 2:错误
    private int type;

    //返回内容(模型回复内容) 如果type=1时返回模型的完整回复内容 如果type=0时返回模型当前回复的片段
    private String content;

    //顺序
    private int seq;

    //异常信息，当type=2时会产生
    private Exception exception;

    /*
     * 当type=1时产生TOKEN计数
     */
    //Token计数 - 用户输入
    private int tokenInput;

    //Token计数 - 输出
    private int tokenOutput;

    //Token计数 - 输出(思考)
    private int tokenThoughtOutput;

}
