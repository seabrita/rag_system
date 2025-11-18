package com.hseabra.demo_rag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class IngestionService {
    private static final int BATCH_SIZE = 100;
    private final ParentDocumentStore parentStore;
    private final PdfService pdfs;
    private final TopicClassifier topics;
    private final ChunkingService chunkingService;
    private final VectorStore vectorStore;

    public IngestResult ingest(String path) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Starting ingestion process for path: {}", path);

        List<Document> pages = pdfs.loadPdf(path);
        log.info("Loaded {} pages from PDF", pages.size());

        String topic = topics.detectTopic(pages);
        log.info("Detected topic: {}", topic);

        String fullContent = "";
        for (Document page : pages) {
            if (page.getText() != null) {
                fullContent = fullContent.concat(page.getText()).concat(" ");
            }
        }

        Map<String, Object> metadata = Map.of("topic", topic);
        List<Document> chunks = chunkingService.createChunks(new Document(fullContent, metadata));
        log.info("Created {} chunks", chunks.size());


        // Add pages to chroma in batches of 300
        for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, chunks.size());
            List<Document> batch = chunks.subList(i, end);
            vectorStore.add(batch);
            log.debug("Added batch {}-{} to vector store", i, end);
        }

        log.info("Ingestion completed in {}s successfully. Topic: {}, Total chunks: {}", (System.currentTimeMillis() - start) / 1000, topic, chunks.size());
        return new IngestResult(topic, chunks.size());
    }

    public IngestResult ingestParent(String path) throws IOException {
        log.info("Starting ingestion process for path: {}", path);

        List<Document> pages = pdfs.loadPdf(path);
        log.info("Loaded {} pages from PDF", pages.size());

        String topic = topics.detectTopic(pages);
        log.info("Detected topic: {}", topic);

        int totalChildren = 0;

        for (Document parentPage : pages) {

            // 1️⃣ Generate parent document ID
            String parentId = UUID.randomUUID().toString();
            log.debug("Generated parent document ID: {}", parentId);

            // 2️⃣ Add metadata
            parentPage.getMetadata().put("parent_id", parentId);
            parentPage.getMetadata().put("topic", topic);
            log.debug("Added metadata to parent document: parent_id={}, topic={}", parentId, topic);

            // 3️⃣ Store full parent document
            parentStore.save(parentId, parentPage);
            log.debug("Stored parent document with ID: {}", parentId);

            // 4️⃣ Chunk to children
            List<Document> childChunks = chunkingService.createChunks(parentPage);
            log.debug("Created {} child chunks for parent ID: {}", childChunks.size(), parentId);

            for (Document child : childChunks) {
                child.getMetadata().put("parent_id", parentId);
                child.getMetadata().put("topic", topic);
            }

            // 5️⃣ Store children in vector DB
            vectorStore.add(childChunks);
            log.debug("Added {} child chunks to vector store for parent ID: {}", childChunks.size(), parentId);

            totalChildren += childChunks.size();
        }

        log.info("Ingestion completed successfully. Topic: {}, Total chunks: {}", topic, totalChildren);
        return new IngestResult(topic, totalChildren);
    }

    public record IngestResult(String topic, int chunks) {
    }
}
