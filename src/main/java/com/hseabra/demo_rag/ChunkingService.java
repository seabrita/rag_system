package com.hseabra.demo_rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {
    private final CustomizedTokenTextSplitter parentSplitter = new CustomizedTokenTextSplitter(1200, 240);

    private final CustomizedTokenTextSplitter childSplitter = new CustomizedTokenTextSplitter(400, 60);
    private final TextSplitter defaultSplitter = TokenTextSplitter.builder().withChunkSize(400).build();

    public List<Document> createChildChunks(Document parent) {
        List<Document> parentChunks = parentSplitter.split(parent);
        List<Document> children = new ArrayList<>();

        for (Document p : parentChunks) {
            List<Document> sub = childSplitter.split(p);
            children.addAll(sub);
        }
        return children;
    }

    public List<Document> createChunks(Document parent) {
        return childSplitter.split(parent);
    }
}
