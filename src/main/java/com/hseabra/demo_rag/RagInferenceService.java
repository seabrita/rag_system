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

        long start = System.currentTimeMillis();
        SearchRequest request = SearchRequest.builder().query(query).topK(5).build();
        List<Document> documents = vectorStore.similaritySearch(request);
        log.info("Inference took {}ms return {} docs", System.currentTimeMillis() - start, documents.size());

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 5️⃣ Build final prompt
        String prompt = """
                You are a helpful assistant that answers user questions using only the provided documents.
                
                Instructions:
                - Use only the information from the retrieved documents.
                - If the answer cannot be found in the documents, respond with:
                  "I don't have enough information to answer that question."
                - Be lyric and erudite.
                
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