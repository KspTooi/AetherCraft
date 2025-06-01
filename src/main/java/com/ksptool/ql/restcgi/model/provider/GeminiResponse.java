package com.ksptool.ql.restcgi.model.provider;

import lombok.Data;
import java.util.List;

@Data
public class GeminiResponse {
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    private UsageMetadata usageMetadata;

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
    }

    @Data
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SafetyRating {
        private String category;
        private String probability;
    }

    @Data
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
    }

    @Data
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
        private Integer thoughtsTokenCount;
    }

    public String getFirstResponseText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.getFirst();
            if (firstCandidate.content != null && 
                firstCandidate.content.parts != null && 
                !firstCandidate.content.parts.isEmpty()) {
                return firstCandidate.content.parts.getFirst().text;
            }
        }
        return null;
    }
} 