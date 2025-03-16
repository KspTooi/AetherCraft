package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.commons.exception.BizException;
import okhttp3.OkHttpClient;

import java.util.function.Consumer;

/**
 * 模型Rest通用接入接口
 * ModelRestCommonInterface
 */
public interface ModelRestCI {

    String sendMessageSync(OkHttpClient client, ModelChatParam dto) throws BizException;

    void sendMessageStream(OkHttpClient client, ModelChatParam param, Consumer<ModelChatContext> callback);

}
