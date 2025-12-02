/**
 * 数据库配置属性
 * 高内聚：所有数据库相关配置集中管理
 * 低耦合：通过配置注入，不硬编码
 */
package com.ll.news.database.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperties {

    /**
     * 数据库类型 - 这是实现多数据库支持的核心
     */
    private DatabaseType type = DatabaseType.MYSQL;

    /**
     * 连接信息
     */
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    /**
     * 连接池配置
     */
    private int maxPoolSize = 20;
    private int minIdle = 5;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
    private long leakDetectionThreshold = 60000;

    /**
     * JPA/Hibernate 配置
     */
    private boolean showSql = false;
    private boolean formatSql = true;
    private int batchSize = 25;

    /**
     * 数据库类型枚举
     * 遵循KISS原则：支持最常用的数据库类型
     */
    public enum DatabaseType {
        MYSQL("com.mysql.cj.jdbc.Driver"),
        POSTGRESQL("org.postgresql.Driver"),
        NEON("org.postgresql.Driver"),  // Neon 使用 PostgreSQL 驱动
        H2("org.h2.Driver");

        private final String defaultDriverClass;

        DatabaseType(String defaultDriverClass) {
            this.defaultDriverClass = defaultDriverClass;
        }

        public String getDefaultDriverClass() {
            return defaultDriverClass;
        }
    }
}