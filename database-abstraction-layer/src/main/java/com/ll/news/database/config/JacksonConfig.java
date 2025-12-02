/**
 * Jackson配置
 * 用于支持PostgreSQL的JSON类型序列化
 * 遵循KISS原则：只做必要的JSON配置
 */
package com.ll.news.database.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper以支持PostgreSQL JSON类型
     * 高内聚：所有JSON配置集中管理
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间模块（支持Instant、LocalDateTime等）
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());

        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 配置JSON序列化特性
        mapper.findAndRegisterModules();

        return mapper;
    }
}