/**
 * 迁移配置属性类
 * 遵循KISS原则：简单、清晰的配置结构
 */
package com.ll.news.migration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    /**
     * 迁移模式
     */
    private Mode mode = Mode.FULL;

    /**
     * 批次大小
     */
    private int batchSize = 1000;

    /**
     * 并发度
     */
    private int parallelism = 4;

    /**
     * 验证配置
     */
    private Validation validation = new Validation();

    /**
     * 回滚配置
     */
    private Rollback rollback = new Rollback();

    /**
     * 性能配置
     */
    private Performance performance = new Performance();

    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();

    /**
     * 源数据库配置
     */
    private DatabaseProperties source = new DatabaseProperties();

    /**
     * 目标数据库配置
     */
    private DatabaseProperties target = new DatabaseProperties();

    /**
     * 调度配置
     */
    private Schedule schedule = new Schedule();

    /**
     * 告警配置
     */
    private Alerting alerting = new Alerting();

    /**
     * 备份配置
     */
    private Backup backup = new Backup();

    /**
     * 数据库配置属性内部类
     */
    @Data
    public static class DatabaseProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private long leakDetectionThreshold = 60000;
    }

    /**
     * 验证配置内部类
     */
    @Data
    public static class Validation {
        private boolean enabled = true;
        private double sampleRate = 0.1;
        private boolean checksumVerification = true;
        private int retryAttempts = 3;
        private long retryDelay = 1000;
    }

    /**
     * 回滚配置内部类
     */
    @Data
    public static class Rollback {
        private boolean enabled = true;
        private int backupRetentionDays = 7;
        private boolean autoRollbackOnFailure = false;
    }

    /**
     * 性能配置内部类
     */
    @Data
    public static class Performance {
        private int readTimeout = 300;  // 秒
        private int writeTimeout = 300; // 秒
        private String memoryLimit = "2G";
        private int fetchSize = 1000;
        private int batchSize = 1000;
    }

    /**
     * 监控配置内部类
     */
    @Data
    public static class Monitoring {
        private boolean enabled = true;
        private int metricsExportInterval = 30;  // 秒
        private int progressReportInterval = 10; // 秒
        private boolean detailedLogging = false;
    }

    /**
     * 调度配置内部类
     */
    @Data
    public static class Schedule {
        private String preCheck = "0 */6 * * * ?";
        private String fullMigration = "0 0 2 * * ?";
        private String incrementalSync = "0 */30 * * * ?";
        private String validation = "0 0 4 * * ?";
    }

    /**
     * 告警配置内部类
     */
    @Data
    public static class Alerting {
        private boolean enabled = true;
        private Email email = new Email();
        private Webhook webhook = new Webhook();
    }

    /**
     * 邮件告警配置内部类
     */
    @Data
    public static class Email {
        private boolean enabled = false;
        private String to;
        private String from;
        private String subject = "数据库迁移告警";
    }

    /**
     * Webhook告警配置内部类
     */
    @Data
    public static class Webhook {
        private boolean enabled = false;
        private String url;
        private String method = "POST";
        private String contentType = "application/json";
    }

    /**
     * 备份配置内部类
     */
    @Data
    public static class Backup {
        private boolean enabled = true;
        private String type = "full"; // full, incremental
        private String location = "/backup/migration/";
        private boolean compression = true;
        private boolean encryption = true;
        private String encryptionKey;
    }

    /**
     * 迁移模式枚举
     */
    public enum Mode {
        FULL,           // 全量迁移
        INCREMENTAL,    // 增量迁移
        VALIDATE,       // 验证模式
        ROLLBACK        // 回滚模式
    }
}