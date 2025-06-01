package com.ksptool.ql.restcgi.model.provider;

import com.google.gson.annotations.SerializedName;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import com.ksptool.ql.restcgi.model.CgiChatParam;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class GrokRequest {

    private List<Message> messages;
    private String model = "grok-2-latest";
    private boolean stream = false;
    private Double temperature;
    
    /**
     * 词汇多样性控制参数，控制模型选词的随机性
     * 数值越小，模型越倾向于选择高概率的词汇；数值越大，词汇选择越多样化
     * 建议只调整此参数或temperature参数中的一个，不要同时调整
     * 
     * @apiNote 取值范围: (0, 1]，默认值: 1
     */
    @SerializedName("top_p")
    private Double topP;
    
    /**
     * 回复内容的最大长度限制（按token计算）
     */
    @SerializedName("max_completion_tokens")
    private Integer maxCompletionTokens;
    
    /**
     * 重复内容惩罚程度，用于避免模型生成重复的句子
     * 正数：减少重复内容；负数：增加重复倾向；0：不进行惩罚
     * 
     * @apiNote 取值范围: [-2, 2]，默认值: 0
     */
    @SerializedName("frequency_penalty")
    private Double frequencyPenalty;

    public GrokRequest() {}


    public GrokRequest(CgiChatParam p) {
        this.model = p.getModel().getCode();
        this.temperature = p.getTemperature();
        this.topP = p.getTopP();
        this.maxCompletionTokens = p.getMaxOutputTokens();
        
        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 添加系统提示词
        if (p.getSystemPrompt() != null && !p.getSystemPrompt().trim().isEmpty()) {
            messages.add(new Message("system", p.getSystemPrompt()));
        }

        // 添加历史消息
        if (p.getHistoryMessages() != null) {
            var sortedMessages = new ArrayList<>(p.getHistoryMessages());
            sortedMessages.sort(Comparator.comparingInt(CgiChatMessage::getSeq));
            
            for (var msg : sortedMessages) {
                String role = msg.getSenderType() == 0 ? "user" : "assistant";
                messages.add(new Message(role, msg.getContent()));
            }
        }

        // 添加当前消息
        if (p.getMessage() != null) {
            messages.add(new Message("user", p.getMessage().getContent()));
        }

        this.messages = messages;
    }

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
        request.setMaxCompletionTokens(maxTokens);
        return request;
    }


    public static GrokRequest ofHistory(List<ModelChatParamHistory> histories, String userMessage, Double temperature, Double topP, Integer maxTokens, String systemPrompt) {
        GrokRequest request = new GrokRequest();
        List<Message> messages = new ArrayList<>();

        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(new Message("system", systemPrompt));
        }

        // 添加历史记录
        if (histories != null && !histories.isEmpty()) {
            for (ModelChatParamHistory history : histories) {
                String role = history.getRole() == 0 ? "user" : "assistant";
                messages.add(new Message(role, history.getContent()));
            }
        }

        // 添加用户的最新消息
        messages.add(new Message("user", userMessage));

        request.setMessages(messages);
        request.setTemperature(temperature);
        request.setTopP(topP);
        request.setMaxCompletionTokens(maxTokens);
        return request;
    }
} 