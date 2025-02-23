package com.ksptool.ql.model.gemini;

import lombok.Data;
import java.util.List;

@Data
public class GeminiRequest {
    private List<Content> contents;

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    public static GeminiRequest of(String text) {
        GeminiRequest request = new GeminiRequest();
        Content content = new Content();
        Part part = new Part();
        part.setText(text);
        content.setParts(List.of(part));
        request.setContents(List.of(content));
        return request;
    }
} 