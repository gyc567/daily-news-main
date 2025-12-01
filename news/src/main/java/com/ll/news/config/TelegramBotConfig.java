package com.ll.news.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Value("${app.telegram.token}")
    String token;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }

}
