package com.ll.news;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Daily News Telegram Bot 启动类
 * 简化版本，专注于Telegram机器人功能
 */
@SpringBootApplication
@EnableAsync
@Slf4j
public class DailyNewsTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyNewsTelegramBotApplication.class, args);
        log.info("🚀 Daily News Telegram Bot 启动成功！");
        log.info("🤖 Telegram Bot Token: 8291537816:AAEQTE7Jd5AGQ9dkq7NMPewlSr8Kun2qXao");
        log.info("📱 机器人命令已启用，可以开始测试了！");
    }
}