package com.ll.news.config;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketClientOptions;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public WebSocketClient webSocketClient(Vertx vertx) {
        return vertx.createWebSocketClient(new WebSocketClientOptions().setSsl(true).setMaxConnections(1000));
    }

}
