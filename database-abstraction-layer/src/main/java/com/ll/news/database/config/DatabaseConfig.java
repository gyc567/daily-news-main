/**
 * 数据库抽象层配置
 * 遵循KISS原则：简单、可配置、可测试
 * 实现高内聚：所有数据库相关配置集中管理
 * 实现低耦合：业务代码不依赖具体数据库实现
 */
package com.ll.news.database.config;

import com.ll.news.database.properties.DatabaseProperties;
import com.ll.news.database.interceptor.QueryPerformanceInterceptor;
import com.ll.news.database.repository.BaseRepositoryImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EntityScan(basePackages = "com.ll.news.database.entity")
@EnableJpaRepositories(
    basePackages = "com.ll.news.database.repository",
    repositoryBaseClass = BaseRepositoryImpl.class
)
public class DatabaseConfig {

    private final DatabaseProperties properties;

    /**
     * 数据源配置 - 支持多种数据库的单一配置
     * KISS原则：一个数据源搞定所有需求
     */
    @Bean
    public DataSource dataSource() {
        log.info("初始化数据源: {} - {}", properties.getType(), properties.getUrl());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());

        // 连接池优化配置
        config.setMaximumPoolSize(properties.getMaxPoolSize());
        config.setMinimumIdle(properties.getMinIdle());
        config.setConnectionTimeout(properties.getConnectionTimeout());
        config.setIdleTimeout(properties.getIdleTimeout());
        config.setMaxLifetime(properties.getMaxLifetime());
        config.setLeakDetectionThreshold(properties.getLeakDetectionThreshold());

        // 数据库特有优化
        configureDatabaseSpecificSettings(config);

        return new HikariDataSource(config);
    }

    /**
     * JPA实体管理器配置
     * 高内聚：所有JPA配置集中管理
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ll.news.database.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * 事务管理器配置
     * 低耦合：统一的事务管理，不依赖具体数据库
     */
    @Bean
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    /**
     * Hibernate属性配置
     * 支持多种数据库的方言自动配置
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();

        // 通用配置
        properties.put("hibernate.dialect", getHibernateDialect());
        properties.put("hibernate.show_sql", this.properties.isShowSql());
        properties.put("hibernate.format_sql", this.properties.isFormatSql());
        properties.put("hibernate.jdbc.batch_size", this.properties.getBatchSize());
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.jdbc.batch_versioned_data", true);

        // 数据库特有配置
        configureDatabaseSpecificHibernateProperties(properties);

        return properties;
    }

    /**
     * 根据数据库类型获取Hibernate方言
     * 这是实现数据库无关性的关键
     */
    private String getHibernateDialect() {
        DatabaseType type = properties.getType();
        switch (type) {
            case MYSQL:
                return "org.hibernate.dialect.MySQL8Dialect";
            case POSTGRESQL:
            case NEON:
                return "org.hibernate.dialect.PostgreSQL15Dialect";
            case H2:
                return "org.hibernate.dialect.H2Dialect";
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + type);
        }
    }

    /**
     * 数据库特有连接池配置
     * 保持KISS：每个数据库的优化集中在一处
     */
    private void configureDatabaseSpecificSettings(HikariConfig config) {
        DatabaseType type = properties.getType();

        switch (type) {
            case NEON:
                // Neon PostgreSQL 特有优化
                config.addDataSourceProperty("ssl", "true");
                config.addDataSourceProperty("sslmode", "require");
                config.addDataSourceProperty("application_name", "daily-news-app");
                config.addDataSourceProperty("socketTimeout", "30");
                break;

            case POSTGRESQL:
                // 标准 PostgreSQL 配置
                config.addDataSourceProperty("stringtype", "unspecified");
                config.addDataSourceProperty("reWriteBatchedInserts", "true");
                break;

            case MYSQL:
                // MySQL 特有配置
                config.addDataSourceProperty("useSSL", "false");
                config.addDataSourceProperty("serverTimezone", "UTC");
                config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
                break;
        }
    }

    /**
     * 数据库特有的Hibernate属性配置
     */
    private void configureDatabaseSpecificHibernateProperties(Properties properties) {
        DatabaseType type = properties.getType();

        if (type == DatabaseType.POSTGRESQL || type == DatabaseType.NEON) {
            // PostgreSQL 特有优化
            properties.put("hibernate.jdbc.lob.non_contextual_creation", true);
            properties.put("hibernate.dialect.storage_engine", "innodb");
            properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);

            // JSON 类型支持（PostgreSQL 优势）
            properties.put("hibernate.types.jackson.object.mapper", "com.ll.news.database.config.JacksonConfig");
        }
    }
}