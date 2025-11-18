package com.hseabra.demo_rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicClassifier {
    private final OpenAiChatModel model;

    public TopicClassifier(OpenAiChatModel model) {
        this.model = model;
    }

    public String detectTopic(List<Document> docs) {

        // join the first 3 documents
        String sample = docs.stream()
                .limit(3)
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // clamp to max 4000 chars
        if (sample.length() > 4000) {
            sample = sample.substring(0, 4000);
        }


        String prompt = """
                Analyze the following document and decide its topic:
                %s
                
                Return a single word such as "bitcoin", "ethereum", or "crypto".
                """.formatted(sample);

        return model.call(prompt).trim().toLowerCase();
    }
}