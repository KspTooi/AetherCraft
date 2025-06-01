package com.ksptool.ql.restcgi.service;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;

import java.util.function.Consumer;

public interface ModelRestCgi {

    CgiChatResult sendMessage(CgiChatParam param) throws BizException;

    void sendMessage(CgiChatParam param, Consumer<CgiChatResult> callback) throws BizException;
}
