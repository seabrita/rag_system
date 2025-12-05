package com.hseabra.demo_rag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rag")
@AllArgsConstructor
@Tag(name = "RAG Operations", description = "Endpoints for RAG (Retrieval-Augmented Generation) operations")
public class RagController {

    private final RagInferenceService ragInferenceService;
    private final IngestionService ingestionService;

    @PostMapping("/inference")
    @Operation(
            summary = "Query the RAG system",
            description = "Submit a question to the RAG system and receive an AI-generated answer based on the ingested documents. Optionally specify an index name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved documents",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InferenceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid inference provided", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<InferenceResponse> inference(
            @Parameter(description = "The question to ask the RAG system and optional index name", required = true)
            @Valid @RequestBody InferenceRequest request) {
        List<Document> documents;
        if (request.getIndexName() != null && !request.getIndexName().isBlank()) {
            documents = ragInferenceService.inference(request.getQuestion(), request.getIndexName());
        } else {
            documents = ragInferenceService.inference(request.getQuestion());
        }

        InferenceResponse response = new InferenceResponse(documents);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/query")
    @Operation(
            summary = "Query the RAG system",
            description = "Submit a question to the RAG system and receive an AI-generated answer based on the ingested documents. Optionally specify an index name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved answer",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query provided", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<QueryResponse> query(
            @Parameter(description = "The question to ask the RAG system and optional index name", required = true)
            @Valid @RequestBody QueryRequest request) {
        String answer;
        if (request.getIndexName() != null && !request.getIndexName().isBlank()) {
            answer = ragInferenceService.query(request.getQuestion(), request.getIndexName());
        } else {
            answer = ragInferenceService.query(request.getQuestion());
        }

        QueryResponse response = new QueryResponse(request.getQuestion(), answer);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ingest")
    @Operation(
            summary = "Ingest a document",
            description = "Ingest a PDF document into the RAG system's vector store. Optionally specify an index name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully ingested document",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IngestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file path provided", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<IngestionResponse> ingest(
            @Parameter(description = "The path to the PDF file to ingest (local path or URL) and optional index name", required = true)
            @Valid @RequestBody IngestionRequest request) {

        ingestionService.ingest(request.getFilePath(), request.getIndexName());

        IngestionResponse response = new IngestionResponse("Document(s) ingested successfully");
        return ResponseEntity.ok(response);
    }

    @Data
    @Schema(description = "Request body for querying the RAG system")
    public static class QueryRequest {
        @NotBlank(message = "Question cannot be blank")
        @Schema(description = "The question to ask", example = "Como Desativar o travamento SAFE?")
        private String question;

        @Schema(description = "Name of the Elasticsearch index to query (optional, defaults to 'test_hugo_index')",
                example = "custom_index_name")
        private String indexName;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "Response containing the answer from the RAG system")
    public static class QueryResponse {
        @Schema(description = "The original question")
        private String question;

        @Schema(description = "The AI-generated answer")
        private String answer;
    }

    @Data
    @Schema(description = "Request body for ingesting a document")
    public static class IngestionRequest {
        @NotEmpty(message = "File path cannot be blank")
        @Schema(
                description = "Path to the PDF file to ingest (supports both local file paths and URLs)",
                example = "https://bitcoin.org/bitcoin.pdf"
        )
        private List<String> filePath;

        @Schema(
                description = "Name of the Elasticsearch index to store the documents (optional, defaults to 'test_hugo_index')",
                example = "custom_index_name"
        )
        private String indexName;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "Response confirming document ingestion")
    public static class IngestionResponse {
        @Schema(description = "Status message")
        private String message;
    }

    @Data
    @Schema(description = "Request body for inference the RAG system")
    public static class InferenceRequest {
        @NotBlank(message = "Question cannot be blank")
        @Schema(description = "The question to ask", example = "Como Desativar o travamento SAFE?")
        private String question;

        @Schema(description = "Name of the Elasticsearch index to query (optional, defaults to 'test_hugo_index')",
                example = "custom_index_name")
        private String indexName;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "Response containing the documents from the RAG system")
    public static class InferenceResponse {
        @Schema(description = "Documents retrieved for the inference")
        private List<Document> documents;

    }
}

