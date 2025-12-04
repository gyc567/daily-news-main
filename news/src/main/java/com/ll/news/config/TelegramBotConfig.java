package com.ll.news.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Telegram机器人配置
 * 支持命令功能和个性化推送配置
 */
@Configuration
public class TelegramBotConfig {

    @Value("${app.telegram.token}")
    String token;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }

    /**
     * Telegram命令配置属性
     */
    @Data
    @ConfigurationProperties(prefix = "app.telegram.commands")
    public static class TelegramCommandsProperties {
        private boolean enabled = true;
        private boolean personalized = true;
        private int defaultFrequency = 30;
        private int maxKeywords = 10;
        private int maxNewsPerPush = 5;
    }
}
