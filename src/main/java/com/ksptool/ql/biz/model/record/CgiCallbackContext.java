package com.ksptool.ql.biz.model.record;

public record CgiCallbackContext(
        Long threadId,
        Long userId,
        Long playerId,
        String streamId,
        String playerName,
        String playerAvatarUrl,
        String modelName,
        String modelAvatarUrl
){}
