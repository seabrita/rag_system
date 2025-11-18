package com.hseabra.demo_rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParentChildRetriever {

    private final VectorStore vectorStore;
    private final ParentDocumentStore parents;

    public ParentChildRetriever(VectorStore vectorStore, ParentDocumentStore parents) {
        this.vectorStore = vectorStore;
        this.parents = parents;
    }

    public List<Document> retrieve(String query) {

        // 1️⃣ Search child chunks
        SearchRequest request = SearchRequest.builder().query(query).topK(5).build();

        List<Document> childResults = vectorStore.similaritySearch(request);

        // 2️⃣ Group by parent_id
        Set<String> parentIds = childResults.stream()
                .map(doc -> doc.getMetadata().get("parent_id").toString())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3️⃣ Fetch parents from in-memory store

        return parentIds.stream()
                .map(parents::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
