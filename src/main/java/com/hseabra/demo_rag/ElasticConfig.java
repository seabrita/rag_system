package com.hseabra.demo_rag;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ElasticConfig {

    @Bean
    public RestClient restClient(@Value("${spring.ai.vectorstore.elasticsearch.cloudId}") String cloudId,
                                 @Value("${spring.ai.vectorstore.elasticsearch.api-key}") String apiKey) {
        log.info("Creating elastic search rest client instance: cloudId={}", cloudId);
        return RestClient.builder(cloudId)
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();
    }
}
