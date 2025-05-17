package com.ksptool.ql.restcgi.model.provider;

import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.restcgi.model.CgiChatParam;
import com.ksptool.ql.restcgi.model.CgiChatMessage;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class GeminiRequest {

    private SystemInstruction systemInstruction;
    private List<Content> contents;
    private List<SafetySetting> safetySettings;
    private GenerationConfig generationConfig;


    public GeminiRequest(){

    }

    public GeminiRequest(CgiChatParam p){

        // 设置系统提示词
        if (StringUtils.isNotBlank(p.getSystemPrompt())) {
            this.systemInstruction = SystemInstruction.of(p.getSystemPrompt());
        }

        // 设置对话内容
        this.contents = new ArrayList<>();
        
        // 添加历史记录
        if (p.getHistoryMessages() != null) {
            
            //根据seq排序
            List<CgiChatMessage> sortedMessages = new ArrayList<>(p.getHistoryMessages());
            sortedMessages.sort(Comparator.comparingInt(CgiChatMessage::getSeq));
            
            for (CgiChatMessage history : sortedMessages) {
                Content content = new Content();
                content.setRole(history.getSenderType() == 0 ? "user" : "model");
                Part part = new Part();
                part.setText(history.getContent());
                content.setParts(List.of(part));
                this.contents.add(content);
            }
        }
        
        // 添加当前消息
        if (p.getMessage() != null) {
            Content content = new Content();
            content.setRole("user");
            Part part = new Part();
            part.setText(p.getMessage().getContent());
            content.setParts(List.of(part));
            this.contents.add(content);
        }

        // 设置安全配置
        this.safetySettings = new ArrayList<>();
        SafetySetting harassmentSetting = new SafetySetting();
        harassmentSetting.setCategory("HARM_CATEGORY_HARASSMENT");
        harassmentSetting.setThreshold("OFF");
        this.safetySettings.add(harassmentSetting);

        SafetySetting hateSpeechSetting = new SafetySetting();
        hateSpeechSetting.setCategory("HARM_CATEGORY_HATE_SPEECH");
        hateSpeechSetting.setThreshold("OFF");
        this.safetySettings.add(hateSpeechSetting);
        
        SafetySetting sexuallySetting = new SafetySetting();
        sexuallySetting.setCategory("HARM_CATEGORY_SEXUALLY_EXPLICIT");
        sexuallySetting.setThreshold("OFF");
        this.safetySettings.add(sexuallySetting);
        
        SafetySetting dangerousSetting = new SafetySetting();
        dangerousSetting.setCategory("HARM_CATEGORY_DANGEROUS_CONTENT");
        dangerousSetting.setThreshold("OFF");
        this.safetySettings.add(dangerousSetting);
        
        SafetySetting civicSetting = new SafetySetting();
        civicSetting.setCategory("HARM_CATEGORY_CIVIC_INTEGRITY");
        civicSetting.setThreshold("BLOCK_NONE");
        this.safetySettings.add(civicSetting);

        // 设置生成配置
        this.generationConfig = new GenerationConfig();
        this.generationConfig.setTemperature(p.getTemperature());
        this.generationConfig.setMaxOutputTokens(p.getMaxOutputTokens());
        this.generationConfig.setTopP(p.getTopP());
        this.generationConfig.setTopK(p.getTopK());
    }

    @Data
    public static class SystemInstruction {
        private List<Part> parts;

        public static SystemInstruction of(String text) {
            SystemInstruction instruction = new SystemInstruction();
            Part part = new Part();
            part.setText(text);
            instruction.setParts(List.of(part));
            return instruction;
        }
    }

    @Data
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SafetySetting {
        private String category;
        private String threshold;
    }

    @Data
    public static class GenerationConfig {
        private List<String> stopSequences;
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;
    }

    public static GeminiRequest of(List<ChatMessage> messages, Double temperature, Double topP, Integer topK, Integer maxOutputTokens) {
        GeminiRequest request = new GeminiRequest();
        
        // 设置对话内容
        List<Content> contents = new ArrayList<>();
        for (ChatMessage message : messages) {
            Content content = new Content();
            content.setRole(message.getRole());
            
            Part part = new Part();
            part.setText(message.getText());
            content.setParts(List.of(part));
            contents.add(content);
        }
        request.setContents(contents);
        
        // 设置安全配置
        List<SafetySetting> safetySettings = new ArrayList<>();
        
        SafetySetting harassmentSetting = new SafetySetting();
        harassmentSetting.setCategory("HARM_CATEGORY_HARASSMENT");
        harassmentSetting.setThreshold("OFF");
        safetySettings.add(harassmentSetting);
        
        SafetySetting hateSpeechSetting = new SafetySetting();
        hateSpeechSetting.setCategory("HARM_CATEGORY_HATE_SPEECH");
        hateSpeechSetting.setThreshold("OFF");
        safetySettings.add(hateSpeechSetting);
        
        SafetySetting sexuallySetting = new SafetySetting();
        sexuallySetting.setCategory("HARM_CATEGORY_SEXUALLY_EXPLICIT");
        sexuallySetting.setThreshold("OFF");
        safetySettings.add(sexuallySetting);
        
        SafetySetting dangerousSetting = new SafetySetting();
        dangerousSetting.setCategory("HARM_CATEGORY_DANGEROUS_CONTENT");
        dangerousSetting.setThreshold("OFF");
        safetySettings.add(dangerousSetting);
        
        SafetySetting civicSetting = new SafetySetting();
        civicSetting.setCategory("HARM_CATEGORY_CIVIC_INTEGRITY");
        civicSetting.setThreshold("BLOCK_NONE");
        safetySettings.add(civicSetting);
        
        request.setSafetySettings(safetySettings);
        
        // 设置生成配置
        GenerationConfig config = new GenerationConfig();
        config.setTemperature(temperature);
        config.setMaxOutputTokens(maxOutputTokens);
        config.setTopP(topP);
        config.setTopK(topK);
        request.setGenerationConfig(config);
        return request;
    }
    
    public static GeminiRequest of(String text, Double temperature, Double topP, Integer topK, Integer maxOutputTokens) {
        return of(List.of(new ChatMessage("user", text)), temperature, topP, topK, maxOutputTokens);
    }
    
    public static GeminiRequest ofHistory(List<ModelChatParamHistory> histories, String userMessage, Double temperature, Double topP, Integer topK, Integer maxOutputTokens) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加历史记录
        if (histories != null && !histories.isEmpty()) {
            for (ModelChatParamHistory history : histories) {
                String role = history.getRole() == 0 ? "user" : "model";
                messages.add(new ChatMessage(role, history.getContent()));
            }
        }
        
        // 添加用户的最新消息
        messages.add(new ChatMessage("user", userMessage));
        return of(messages, temperature, topP, topK, maxOutputTokens);
    }

    public static GeminiRequest ofHistory(List<ModelChatParamHistory> histories, String userMessage, Double temperature, Double topP, Integer topK, Integer maxOutputTokens, String systemPrompt) {
        GeminiRequest request = ofHistory(histories, userMessage, temperature, topP, topK, maxOutputTokens);
        
        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            request.setSystemInstruction(SystemInstruction.of(systemPrompt));
        }
        
        return request;
    }
    
    @Data
    public static class ChatMessage {
        private String role;
        private String text;
        
        public ChatMessage(String role, String text) {
            this.role = role;
            this.text = text;
        }
    }
} 