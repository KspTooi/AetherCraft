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

    //返回内容(模型思考内容) 当type=50时一次性返回 当type=0时使用content字段返回思考片段
    private String contentThought;

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

    public static CgiChatResult thought(CgiChatParam param,String content,int seq){
        var ccr = new CgiChatResult();
        ccr.setModel(param.getModel());
        ccr.setType(0); //0:思考片段 1:文本 50:结束 51:错误
        ccr.setContent(content);
        ccr.setContentThought(null);
        ccr.setSeq(seq);
        ccr.setException(null);
        ccr.setTokenInput(0);
        ccr.setTokenOutput(0);
        ccr.setTokenThoughtOutput(0);
        return ccr;
    }

    public static CgiChatResult text(CgiChatParam param,String content,int seq){
        var ccr = new CgiChatResult();
        ccr.setModel(param.getModel());
        ccr.setType(1); //0:思考片段 1:文本 50:结束 51:错误
        ccr.setContent(content);
        ccr.setContentThought(null);
        ccr.setSeq(seq);
        ccr.setException(null);
        ccr.setTokenInput(0);
        ccr.setTokenOutput(0);
        ccr.setTokenThoughtOutput(0);
        return ccr;
    }

    public static CgiChatResult error(CgiChatParam param,String content,Exception exception){
        var ccr = new CgiChatResult();
        ccr.setModel(param.getModel());
        ccr.setType(51); //0:思考片段 1:文本 50:结束 51:错误
        ccr.setContent(content);
        ccr.setContentThought(null);
        ccr.setSeq(-1);
        ccr.setException(exception);
        ccr.setTokenInput(0);
        ccr.setTokenOutput(0);
        ccr.setTokenThoughtOutput(0);
        return ccr;
    }

    public static CgiChatResult finish(CgiChatParam param,String allContent,String allThoughtContent,int seq){
        var ccr = new CgiChatResult();
        ccr.setModel(param.getModel());
        ccr.setType(50); //0:思考片段 1:文本 50:结束 51:错误
        ccr.setContent(allContent);
        ccr.setContentThought(allThoughtContent);
        ccr.setSeq(seq);
        ccr.setException(null);
        ccr.setTokenInput(0);
        ccr.setTokenOutput(0);
        ccr.setTokenThoughtOutput(0);
        return ccr;
    }



}
