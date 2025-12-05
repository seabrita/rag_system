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

    private static final String DEFAULT_INDEX = "test_hugo_index";
    private final ElasticConfig.VectorStoreFactory vectorStoreFactory;
    private final ChatModel chat;

    public List<Document> inference(String query) {
        return inference(query, DEFAULT_INDEX);
    }

    public List<Document> inference(String query, String indexName) {
        log.info("Received query: {} for index: {}", query, indexName);
        long start = System.currentTimeMillis();
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.6)
                .build();

        VectorStore vectorStore = vectorStoreFactory.getVectorStore(indexName);
        List<Document> documents = vectorStore.similaritySearch(request);
        for (Document document : documents) {
            log.info("Retrieved doc with metadata={}\n{}", document.getMetadata(), document.getText());
        }
        log.info("Inference took {}ms return {} docs", System.currentTimeMillis() - start, documents.size());
        return documents;
    }

    public String query(String query) {
        return query(query, DEFAULT_INDEX);
    }

    public String query(String query, String indexName) {
        List<Document> documents = inference(query, indexName);
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 5️⃣ Build final prompt
        String prompt = """
                You are a helpful assistant that answers user questions using ONLY the information in the Documents.
                
                RULES (read carefully):
                1. You MUST NOT use any information that is not explicitly present in the Documents.
                2. If the answer is not supported by the Documents, respond with EXACTLY:
                   "I don't have enough information to answer that question."
                3. Do NOT use prior knowledge.
                
                Documents:
                %s
                
                User question:
                %s
                
                Answer:
                """.formatted(context, query);

        // 6️⃣ Generate answer
        return chat.call(prompt).trim();
    }
}