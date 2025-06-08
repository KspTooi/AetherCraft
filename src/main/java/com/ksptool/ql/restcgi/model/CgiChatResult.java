package com.ksptool.ql.restcgi.model;

import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import lombok.Data;

@Data
public class CgiChatResult {

    //模型枚举
    private ModelVariantSchema model;

    //返回类型(旧) 0:数据 1:结束 2:错误

    //返回类型(新) 0:思考片段 1:文本 50:结束 51:错误
    private int type;

    //返回内容(模型回复内容) 如果type=50时返回模型的完整回复内容 如果type=0/1时返回模型当前回复的片段
    private String content;

    //顺序
    private int seq;

    //异常信息，当type=51时会产生
    private Exception exception;

    /*
     * 当type=50时产生TOKEN计数
     */
    //Token计数 - 用户输入
    private int tokenInput;

    //Token计数 - 输出
    private int tokenOutput;

    //Token计数 - 输出(思考)
    private int tokenThoughtOutput;

    public void setTokenInput(Integer tokenInput) {
        if (tokenInput == null) {
            this.tokenInput = 0;
            return;
        }
        this.tokenInput = tokenInput;
    }

    public void setTokenOutput(Integer tokenOutput) {
        if (tokenOutput == null) {
            this.tokenOutput = 0;
            return;
        }
        this.tokenOutput = tokenOutput;
    }

    public void setTokenThoughtOutput(Integer tokenThoughtOutput) {
        if (tokenThoughtOutput == null) {
            this.tokenThoughtOutput = 0;
            return;
        }
        this.tokenThoughtOutput = tokenThoughtOutput;
    }
}
