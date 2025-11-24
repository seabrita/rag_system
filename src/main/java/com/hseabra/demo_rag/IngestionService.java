package com.hseabra.demo_rag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@AllArgsConstructor
public class IngestionService {
    private static final int BATCH_SIZE = 50;
    private static final int PARALLELISM = 10;
    private final PdfService pdfs;
    private final TopicClassifier topics;
    private final ChunkingService chunkingService;
    private final VectorStore vectorStore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(PARALLELISM);

    public void ingest(String path) {
        long start = System.currentTimeMillis();
        log.info("Starting ingestion process for path: {}", path);

        List<Document> pages;
        try {
            pages = pdfs.loadPdf(path);
        } catch (IOException e) {
            log.error("Failed to load PDF from path: {}", path, e);
            return;
        }
        log.info("PDF with '{}' pages loaded in {}ms", pages.size(), System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        String topic = topics.detectTopic(pages);
        log.info("Topic '{}' detected in {}ms", topic, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        String fullContent = "";
        for (Document page : pages) {
            if (page.getText() != null) {
                fullContent = fullContent.concat(page.getText()).concat(" ");
            }
        }

        Map<String, Object> metadata = Map.of("topic", topic, "path", path, "knowledge_bases", List.of("general", "pdfs"));
        List<Document> chunks = chunkingService.createChunks(new Document(fullContent, metadata));
        log.info("'{}' chunks created in {}ms", chunks.size(), System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        AtomicLong timeSum = new AtomicLong(0);
        AtomicInteger count = new AtomicInteger(0);

        // Parallelize batch insertion to vector store
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
            final int batchStart = i;
            final int batchEnd = Math.min(i + BATCH_SIZE, chunks.size());
            final List<Document> batch = new ArrayList<>(chunks.subList(batchStart, batchEnd));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    long l = System.currentTimeMillis();
                    vectorStore.add(batch);
                    timeSum.updateAndGet(v -> v + (System.currentTimeMillis() - l));
                    count.addAndGet(1);
                } catch (Exception e) {
                    log.error("Error adding batch {}-{} to vector store", batchStart, batchEnd, e);
                    throw new RuntimeException("Failed to add batch to vector store", e);
                }
            }, forkJoinPool);

            futures.add(future);
        }

        // Wait for all batches to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Ingestion completed in {}s successfully. Topic: {}, Total chunks: {}, ES calls: {}, AVG time per call: {}ms",
                (System.currentTimeMillis() - start) / 1000, topic, chunks.size(), count.get(), timeSum.get() / Math.max(1, count.get()));
    }

    public void ingest(List<String> filesPath) {
        filesPath.forEach(x -> executor.execute(() -> ingest(x)));
    }
}
