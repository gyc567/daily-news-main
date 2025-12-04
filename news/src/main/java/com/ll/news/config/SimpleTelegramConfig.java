package com.ll.news.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 简化的Telegram Bot配置
 * 专注于核心机器人功能
 */
@Configuration
public class SimpleTelegramConfig {

    @Value("${app.telegram.token:8291537816:AAEQTE7Jd5AGQ9dkq7NMPewlSr8Kun2qXao}")
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }
}