package com.ksptool.ql.biz.model.gemini;

import lombok.Data;
import java.util.List;

@Data
public class GeminiRequest {
    private List<Content> contents;
    private List<SafetySetting> safetySettings;
    private GenerationConfig generationConfig;

    @Data
    public static class Content {
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

    public static GeminiRequest of(String text, Double temperature, Double topP, Integer topK) {
        GeminiRequest request = new GeminiRequest();
        
        // 设置内容
        Content content = new Content();
        Part part = new Part();
        part.setText(text);
        content.setParts(List.of(part));
        request.setContents(List.of(content));
        
        // 设置安全配置
        SafetySetting safetySetting = new SafetySetting();
        safetySetting.setCategory("HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySetting.setThreshold("BLOCK_ONLY_HIGH");
        request.setSafetySettings(List.of(safetySetting));
        
        // 设置生成配置
        GenerationConfig config = new GenerationConfig();
        config.setTemperature(temperature);
        config.setMaxOutputTokens(800);
        config.setTopP(topP);
        config.setTopK(topK);
        request.setGenerationConfig(config);
        
        return request;
    }
} 