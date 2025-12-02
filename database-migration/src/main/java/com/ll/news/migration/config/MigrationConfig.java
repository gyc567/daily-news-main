/**
 * 迁移配置类
 * 配置数据源和迁移相关的Bean
 */
package com.ll.news.migration.config;

import com.ll.news.migration.properties.MigrationProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
@EnableConfigurationProperties(MigrationProperties.class)
public class MigrationConfig {

    private final MigrationProperties properties;

    /**
     * 源数据库数据源 (MySQL)
     */
    @Bean("sourceDataSource")
    @Primary
    public DataSource sourceDataSource() {
        log.info("配置源数据库连接 (MySQL): {}", properties.getSource().getUrl());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getSource().getUrl());
        config.setUsername(properties.getSource().getUsername());
        config.setPassword(properties.getSource().getPassword());
        config.setDriverClassName(properties.getSource().getDriverClassName());

        // MySQL 特有配置
        config.setPoolName("SourceMySQLPool");
        config.setMaximumPoolSize(properties.getSource().getMaxPoolSize());
        config.setMinimumIdle(properties.getSource().getMinIdle());
        config.setConnectionTimeout(properties.getSource().getConnectionTimeout());

        // MySQL 性能优化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    /**
     * 目标数据库数据源 (Neon PostgreSQL)
     */
    @Bean("targetDataSource")
    public DataSource targetDataSource() {
        log.info("配置目标数据库连接 (Neon PostgreSQL): {}", properties.getTarget().getUrl());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getTarget().getUrl());
        config.setUsername(properties.getTarget().getUsername());
        config.setPassword(properties.getTarget().getPassword());
        config.setDriverClassName(properties.getTarget().getDriverClassName());

        // Neon 特有配置
        config.setPoolName("TargetNeonPool");
        config.setMaximumPoolSize(properties.getTarget().getMaxPoolSize());
        config.setMinimumIdle(properties.getTarget().getMinIdle());
        config.setConnectionTimeout(properties.getTarget().getConnectionTimeout());

        // SSL 配置
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        config.addDataSourceProperty("application_name", "daily-news-migration");

        // PostgreSQL 性能优化
        config.addDataSourceProperty("stringtype", "unspecified");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("defaultAutoCommit", "false");

        // 连接保持
        config.addDataSourceProperty("tcpKeepAlive", "true");

        return new HikariDataSource(config);
    }

    /**
     * 源数据库 JdbcTemplate
     */
    @Bean("sourceJdbcTemplate")
    public JdbcTemplate sourceJdbcTemplate() {
        return new JdbcTemplate(sourceDataSource());
    }

    /**
     * 源数据库 NamedParameterJdbcTemplate
     */
    @Bean("sourceNamedJdbcTemplate")
    public NamedParameterJdbcTemplate sourceNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(sourceDataSource());
    }

    /**
     * 目标数据库 JdbcTemplate
     */
    @Bean("targetJdbcTemplate")
    public JdbcTemplate targetJdbcTemplate() {
        return new JdbcTemplate(targetDataSource());
    }

    /**
     * 目标数据库 NamedParameterJdbcTemplate
     */
    @Bean("targetNamedJdbcTemplate")
    public NamedParameterJdbcTemplate targetNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(targetDataSource());
    }
}