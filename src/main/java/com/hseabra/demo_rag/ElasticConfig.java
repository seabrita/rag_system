package com.hseabra.demo_rag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@AllArgsConstructor
@Slf4j
public class ElasticConfig {
    @Bean
    public RestClient restClient(@Value("${spring.ai.vectorstore.elasticsearch.cloudId}") String cloudId,
                                 @Value("${spring.ai.vectorstore.elasticsearch.apiKey}") String apiKey) {
        log.info("Creating elastic search rest client instance: cloudId={}", cloudId);
        return RestClient.builder(cloudId)
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();
    }

    @Bean
    public VectorStoreFactory vectorStoreFactory(RestClient restClient, EmbeddingModel embeddingModel, ElasticsearchVectorStoreProperties properties) {
        return new VectorStoreFactory(restClient, embeddingModel, properties);
    }

    /**
     * Factory for creating VectorStore instances with different index names
     */
    public static class VectorStoreFactory {
        private final RestClient restClient;
        private final EmbeddingModel embeddingModel;
        private final ElasticsearchVectorStoreProperties properties;
        private final Map<String, VectorStore> vectorStoreCache = new ConcurrentHashMap<>();

        public VectorStoreFactory(RestClient restClient, EmbeddingModel embeddingModel, ElasticsearchVectorStoreProperties properties) {
            this.restClient = restClient;
            this.embeddingModel = embeddingModel;
            this.properties = properties;
        }

        /**
         * Get or create a VectorStore for the specified index
         */
        public VectorStore getVectorStore(String indexName) {
            return vectorStoreCache.computeIfAbsent(indexName, key -> {
                log.info("Creating VectorStore for index: {}", indexName);
                ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
                options.setIndexName(indexName);
                options.setDimensions(properties.getDimensions());
                options.setSimilarity(properties.getSimilarity());
                return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                        .options(options)
                        .initializeSchema(properties.isInitializeSchema())
                        .batchingStrategy(new TokenCountBatchingStrategy())
                        .build();
            });
        }
    }
}
