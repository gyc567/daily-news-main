package com.ll.news.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据库迁移应用主类
 * 负责MySQL到Neon PostgreSQL的迁移
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan("com.ll.news.migration")
public class Application {

    public static void main(String[] args) {
        log.info("🚀 启动 Daily News 数据库迁移工具");
        log.info("📊 迁移目标: MySQL -> Neon PostgreSQL");
        log.info("🔧 连接池地址: ep-morning-wind-aho6ug36-pooler.c-3.us-east-1.aws.neon.tech");

        try {
            SpringApplication.run(Application.class, args);
            log.info("✅ 数据库迁移工具启动成功");
        } catch (Exception e) {
            log.error("❌ 数据库迁移工具启动失败", e);
            System.exit(1);
        }
    }
}