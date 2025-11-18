package com.hseabra.demo_rag;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
@Slf4j
public class AiConfig {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Bean
    public ElasticsearchClient elasticsearchClient(
            @Value("${spring.elasticsearch.uris:http://localhost:9200}") String uri,
            @Value("${spring.elasticsearch.username:elastic}") String username,
            @Value("${spring.elasticsearch.password:}") String password) {

        log.info("Creating Elasticsearch client for URI: {}", uri);

        // Parse URI
        HttpHost host = HttpHost.create(uri);

        // Create credentials provider
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        // Create the low-level client
        RestClient restClient = RestClient.builder(host)
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();

        // Create the transport with a Jackson mapper
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
/*
    @Bean
    public ChromaVectorStore vectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi,
                                         ChromaVectorStoreProperties storeProperties, ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
                                         BatchingStrategy chromaBatchingStrategy, OpenAiEmbeddingProperties openAiEmbeddingProperties) {
        String collectionName = storeProperties.getCollectionName() + "_collection";// + LocalDateTime.now().format(DTF);
        //ChromaApi.CreateCollectionRequest request = new ChromaApi.CreateCollectionRequest(collectionName);
        //ChromaApi.Collection collection = chromaApi.createCollection(storeProperties.getTenantName(), storeProperties.getDatabaseName(), request);
        //log.info("Created Chroma collection: {}", collection.name());

        String model = openAiEmbeddingProperties.getOptions().getModel();
        Integer dimensions = openAiEmbeddingProperties.getOptions().getDimensions();
        log.info("Created Chroma model: {}, dimensions: {}", model, dimensions);
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .databaseName(storeProperties.getDatabaseName())
                .tenantName(storeProperties.getTenantName())
                .initializeSchema(storeProperties.isInitializeSchema())
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
                .batchingStrategy(chromaBatchingStrategy)
                .build();
    }
 */
}
