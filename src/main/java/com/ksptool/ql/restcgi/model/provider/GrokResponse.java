package com.ksptool.ql.restcgi.model.provider;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("system_fingerprint")
    private String systemFingerprint;

    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        private Message delta;
        @SerializedName("finish_reason")
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
        @SerializedName("prompt_tokens")
        private Integer promptTokens;
        @SerializedName("completion_tokens")
        private Integer completionTokens;
        @SerializedName("total_tokens")
        private Integer totalTokens;
        @SerializedName("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;
        @SerializedName("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;
    }

    @Data
    public static class PromptTokensDetails {
        @SerializedName("text_tokens")
        private Integer textTokens;
        @SerializedName("audio_tokens")
        private Integer audioTokens;
        @SerializedName("image_tokens")
        private Integer imageTokens;
        @SerializedName("cached_tokens")
        private Integer cachedTokens;
    }

    @Data
    public static class CompletionTokensDetails {
        @SerializedName("reasoning_tokens")
        private Integer reasoningTokens;
        @SerializedName("audio_tokens")
        private Integer audioTokens;
        @SerializedName("accepted_prediction_tokens")
        private Integer acceptedPredictionTokens;
        @SerializedName("rejected_prediction_tokens")
        private Integer rejectedPredictionTokens;
    }
} 