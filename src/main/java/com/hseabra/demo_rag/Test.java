package com.hseabra.demo_rag;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class Test {
    private final IngestionService ingestionService;
    private final RagInferenceService ragInferenceService;

    @PostConstruct
    public void init() throws IOException {
        /*
        EmbeddingRequest request = new EmbeddingRequest(List.of("hugo", "ana", "teste"), null);
        EmbeddingResponse res = openAiEmbeddingModel.call(request);
        log.info("Sample embeddings at startup: {}", res.getResult().getOutput());
        log.info("Sample embeddings at startup: {}", res.getMetadata().getModel());
        log.info("Sample embeddings at startup: {}", res.getMetadata().getUsage());

        String call = openAiChatModel.call("where is Ancas, Anadia?");
        log.info("Sample chat call at startup: {}", call);
        */


        // OLD method (keep for comparison)
        ingestionService.ingest("src/main/resources/hugo_22mb_389pages.pdf");
        String inference = ragInferenceService.inference("Como Desativar o travamento SAFE?");
        log.info("Inference result: {}", inference);
    }
}
