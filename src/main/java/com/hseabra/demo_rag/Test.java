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
        ingestionService.ingest("src/main/resources/hugo_22mb_389pages.pdf");
        String inference = ragInferenceService.inference("Como Desativar o travamento SAFE?");
        log.info("Inference result: {}", inference);
    }
}
