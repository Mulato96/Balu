package com.gal.afiliaciones.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Configuration
@ConfigurationProperties(prefix = "registraduria")
public class RegistraduriaConfig {

    private Soap soap = new Soap();
    private Timeout timeout = new Timeout();

    @Data
    public static class Soap {
        private String url;
        private String action;
    }

    @Data
    public static class Timeout {
        private int connection = 30;
        private int read = 30;
    }

    @Bean
    public WebClient registraduriaWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
} 