package com.ksptool.ql.biz.model.grok;

import com.ksptool.ql.biz.model.po.ModelChatHistoryPo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GrokRequest {
    private List<Message> messages;
    private String model = "grok-2-latest";
    private boolean stream = false;
    private Double temperature;
    private Double topP;
    private Integer maxTokens;

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static GrokRequest of(String text, Double temperature, Double topP, Integer maxTokens) {
        GrokRequest request = new GrokRequest();
        request.setMessages(List.of(new Message("user", text)));
        request.setTemperature(temperature);
        request.setTopP(topP);
        request.setMaxTokens(maxTokens);
        return request;
    }

    public static GrokRequest ofHistory(List<ModelChatHistoryPo> histories, String userMessage, Double temperature, Double topP, Integer maxTokens) {
        GrokRequest request = new GrokRequest();
        List<Message> messages = new ArrayList<>();

        // 添加历史记录
        if (histories != null && !histories.isEmpty()) {
            for (ModelChatHistoryPo history : histories) {
                String role = history.getRole() == 0 ? "user" : "assistant";
                messages.add(new Message(role, history.getContent()));
            }
        }

        // 添加用户的最新消息
        messages.add(new Message("user", userMessage));

        request.setMessages(messages);
        request.setTemperature(temperature);
        request.setTopP(topP);
        request.setMaxTokens(maxTokens);
        return request;
    }

    public static GrokRequest ofHistory(List<ModelChatHistoryPo> histories, String userMessage, Double temperature, Double topP, Integer maxTokens, String systemPrompt) {
        GrokRequest request = new GrokRequest();
        List<Message> messages = new ArrayList<>();

        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(new Message("system", systemPrompt));
        }

        // 添加历史记录
        if (histories != null && !histories.isEmpty()) {
            for (ModelChatHistoryPo history : histories) {
                String role = history.getRole() == 0 ? "user" : "assistant";
                messages.add(new Message(role, history.getContent()));
            }
        }

        // 添加用户的最新消息
        messages.add(new Message("user", userMessage));

        request.setMessages(messages);
        request.setTemperature(temperature);
        request.setTopP(topP);
        request.setMaxTokens(maxTokens);
        return request;
    }
} 