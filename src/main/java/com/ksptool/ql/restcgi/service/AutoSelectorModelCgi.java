package com.ksptool.ql.restcgi.service;

import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatResult;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import java.util.function.Consumer;

@Primary
@Service
public class AutoSelectorModelCgi implements ModelRestCgi{

    @Override
    public CgiChatResult sendMessage(CgiChatParam param) {
        return null;
    }

    @Override
    public void sendMessage(CgiChatParam param, Consumer<CgiChatResult> callback) {

    }
    
}
