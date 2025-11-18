package com.hseabra.demo_rag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RagInferenceService {

    private final VectorStore vectorStore;
    private final ChatModel chat;

    public String inference(String query) {

        SearchRequest request = SearchRequest.builder().query(query).topK(3).build();
        List<Document> documents = vectorStore.similaritySearch(request);
        log.info("Inference return {} chunks", documents.size());
        for (Document document : documents) {
            log.info("\tscore {}", document.getScore());
            log.info("\tchunk {}/{}", document.getMetadata().get("chunk_index"), document.getMetadata().get("total_chunks"));
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 5️⃣ Build final prompt
        String prompt = """
                You are a helpful assistant answering questions about cryptocurrency whitepapers.
                
                Use the conversation history and the retrieved documents to answer.
                
                If the answer is not found in the documents, say:
                "I don't have enough information to answer that question."
                
                Retrieved documents:
                %s
                
                User question:
                %s
                
                Answer:
                """.formatted(context, query);

        // 6️⃣ Generate answer
        return chat.call(prompt).trim();
    }
}