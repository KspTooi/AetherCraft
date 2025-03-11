package com.ksptool.ql.biz.model.grok;

import lombok.Data;
import java.util.List;

@Data
public class GrokResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String systemFingerprint;

    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        private Message delta;
        private String finishReason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
        private String refusal;
    }

    @Data
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private Integer reasoningTokens;
        private PromptTokensDetails promptTokensDetails;
    }

    @Data
    public static class PromptTokensDetails {
        private Integer textTokens;
        private Integer audioTokens;
        private Integer imageTokens;
        private Integer cachedTokens;
    }
} 