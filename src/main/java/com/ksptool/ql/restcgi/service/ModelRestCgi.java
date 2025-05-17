package com.ksptool.ql.restcgi.service;

import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;

import java.util.function.Consumer;

public interface ModelRestCgi {

    CgiChatResult sendMessage(CgiChatParam param);

    void sendMessage(CgiChatParam param, Consumer<CgiChatResult> callback);
}
