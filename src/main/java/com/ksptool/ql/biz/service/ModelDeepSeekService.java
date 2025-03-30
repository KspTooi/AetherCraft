package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.commons.exception.BizException;
import okhttp3.OkHttpClient;

import java.util.function.Consumer;

public class ModelDeepSeekService implements ModelRestCI {

    @Override
    public String sendMessageSync(OkHttpClient client, ModelChatParam dto) throws BizException {
        return "";
    }

    @Override
    public String sendMessageStream(OkHttpClient client, ModelChatParam param, Consumer<ModelChatContext> callback) {
        return null;
    }
}
