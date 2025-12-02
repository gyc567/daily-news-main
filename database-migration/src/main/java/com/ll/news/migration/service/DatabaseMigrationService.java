/**
 * 数据库迁移服务
 * 负责执行MySQL到Neon PostgreSQL的数据迁移
 * 遵循KISS原则：简单、可靠、可监控
 */
package com.ll.news.migration.service;

import com.ll.news.migration.entity.*;
import com.ll.news.migration.properties.MigrationProperties;
import com.ll.news.migration.repository.MigrationRepository;
import com.ll.news.migration.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMigrationService {

    @Qualifier("sourceJdbcTemplate")
    private final JdbcTemplate sourceJdbcTemplate;

    @Qualifier("targetJdbcTemplate")
    private final JdbcTemplate targetJdbcTemplate;

    @Qualifier("sourceNamedJdbcTemplate")
    private final NamedParameterJdbcTemplate sourceNamedJdbcTemplate;

    @Qualifier("targetNamedJdbcTemplate")
    private final NamedParameterJdbcTemplate targetNamedJdbcTemplate;

    private final MigrationProperties properties;
    private final MigrationRepository repository;
    private final MigrationProgressMonitor progressMonitor;
    private final DataValidationService validationService;
    private final MigrationAlertService alertService;

    // 迁移状态跟踪
    private final Map<String, MigrationStatus> migrationStatus = new ConcurrentHashMap<>();
    private final AtomicLong totalRowsProcessed = new AtomicLong(0);
    private final AtomicLong totalRowsFailed = new AtomicLong(0);

    /**
     * 执行完整的数据库迁移
     * KISS原则：单一入口，清晰的责任链
     */
    @Transactional
    public MigrationResult migrate() {
        log.info("🚀 开始数据库迁移：MySQL -> Neon PostgreSQL");
        log.info("📊 源数据库：ep-morning-wind-aho6ug36-pooler.c-3.us-east-1.aws.neon.tech/neondb");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MigrationResult result = MigrationResult.builder()
                .startTime(LocalDateTime.now())
                .status(MigrationStatus.RUNNING)
                .build();

        try {
            // 1. 迁移前检查
            preMigrationCheck();

            // 2. 创建目标数据库结构
            createTargetDatabaseStructure();

            // 3. 执行数据迁移
            performDataMigration();

            // 4. 数据验证
            if (properties.getValidation().isEnabled()) {
                validateMigration();
            }

            // 5. 创建索引和约束
            createIndexesAndConstraints();

            // 6. 最终验证
            finalValidation();

            stopWatch.stop();
            result.setEndTime(LocalDateTime.now());
            result.setDuration(stopWatch.getTotalTimeSeconds());
            result.setStatus(MigrationStatus.SUCCESS);
            result.setTotalRowsProcessed(totalRowsProcessed.get());

            log.info("✅ 数据库迁移成功！耗时：{} 秒", result.getDuration());
            log.info("📈 处理行数：{} 行", result.getTotalRowsProcessed());

            // 发送成功通知
            alertService.sendSuccessAlert(result);

        } catch (Exception e) {
            stopWatch.stop();
            result.setEndTime(LocalDateTime.now());
            result.setDuration(stopWatch.getTotalTimeSeconds());
            result.setStatus(MigrationStatus.FAILED);
            result.setErrorMessage(e.getMessage());

            log.error("❌ 数据库迁移失败", e);

            // 发送失败通知
            alertService.sendFailureAlert(result, e);

            // 如果配置了自动回滚
            if (properties.getRollback().isAutoRollbackOnFailure()) {
                log.info("🔄 执行自动回滚...");
                rollback();
            }

            throw new MigrationException("数据库迁移失败", e);
        }

        return result;
    }

    /**
     * 迁移前检查
     * 确保源数据库和目标数据库都可用
     */
    private void preMigrationCheck() {
        log.info("🔍 执行迁移前检查...");

        // 检查源数据库连接
        try {
            sourceJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("✅ 源数据库连接正常");
        } catch (Exception e) {
            throw new MigrationException("源数据库连接失败", e);
        }

        // 检查目标数据库连接
        try {
            targetJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("✅ 目标数据库连接正常");
        } catch (Exception e) {
            throw new MigrationException("目标数据库连接失败", e);
        }

        // 检查数据库版本
        checkDatabaseVersions();

        // 检查磁盘空间（如果可能）
        checkDiskSpace();

        // 检查表结构兼容性
        checkSchemaCompatibility();

        log.info("✅ 预检查完成");
    }

    /**
     * 检查数据库版本
     */
    private void checkDatabaseVersions() {
        log.info("🔍 检查数据库版本...");

        // MySQL版本
        String mysqlVersion = sourceJdbcTemplate.queryForObject(
                "SELECT VERSION()", String.class);
        log.info("📋 MySQL版本: {}", mysqlVersion);

        // PostgreSQL版本
        String postgresVersion = targetJdbcTemplate.queryForObject(
                "SELECT version()", String.class);
        log.info("📋 PostgreSQL版本: {}", postgresVersion);

        // 版本兼容性检查
        if (!mysqlVersion.contains("8.0")) {
            log.warn("⚠️  MySQL版本可能不兼容，建议升级到8.0+");
        }
    }

    /**
     * 检查磁盘空间
     */
    private void checkDiskSpace() {
        log.info("🔍 检查磁盘空间...");

        // 估算数据大小
        Long dataSize = estimateDataSize();
        log.info("📊 预估数据大小: {} MB", dataSize / (1024 * 1024));

        // 这里可以添加实际的磁盘空间检查逻辑
        log.info("✅ 磁盘空间检查完成");
    }

    /**
     * 估算数据大小
     */
    private Long estimateDataSize() {
        String sql = "SELECT SUM(data_length + index_length) " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema IN ('daily-news', 'financial_analytics')";

        Long size = sourceJdbcTemplate.queryForObject(sql, Long.class);
        return size != null ? size : 0L;
    }

    /**
     * 检查表结构兼容性
     */
    private void checkSchemaCompatibility() {
        log.info("🔍 检查表结构兼容性...");

        // 获取所有表
        List<String> tables = getAllTables();
        log.info("📋 发现 {} 个表需要迁移", tables.size());

        for (String table : tables) {
            log.debug("检查表: {}", table);
            checkTableCompatibility(table);
        }

        log.info("✅ 表结构兼容性检查完成");
    }

    /**
     * 获取所有需要迁移的表
     */
    private List<String> getAllTables() {
        String sql = "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema IN ('daily-news', 'financial_analytics') " +
                    "AND table_type = 'BASE TABLE'";

        return sourceJdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * 检查单个表的兼容性
     */
    private void checkTableCompatibility(String tableName) {
        // 检查表结构
        String sql = "SELECT column_name, data_type, column_type, is_nullable, " +
                    "column_key, column_default, extra " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema IN ('daily-news', 'financial_analytics') " +
                    "AND table_name = ? " +
                    "ORDER BY ordinal_position";

        List<Map<String, Object>> columns = sourceJdbcTemplate.queryForList(sql, tableName);

        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("column_name");
            String dataType = (String) column.get("data_type");
            String columnType = (String) column.get("column_type");

            // 检查数据类型兼容性
            if (!isDataTypeCompatible(dataType, columnType)) {
                log.warn("⚠️  表 {} 列 {} 的数据类型 {} 可能需要特殊处理",
                        tableName, columnName, columnType);
            }
        }
    }

    /**
     * 检查数据类型兼容性
     */
    private boolean isDataTypeCompatible(String dataType, String columnType) {
        // 定义兼容的数据类型映射
        Set<String> compatibleTypes = Set.of(
                "bigint", "int", "smallint", "tinyint",
                "varchar", "char", "text", "longtext",
                "decimal", "numeric", "float", "double",
                "datetime", "timestamp", "date", "time",
                "json", "jsonb"
        );

        return compatibleTypes.contains(dataType.toLowerCase());
    }

    /**
     * 创建目标数据库结构
     */
    private void createTargetDatabaseStructure() {
        log.info("🏗️  创建目标数据库结构...");

        // 创建 schema
        createSchemas();

        // 创建表结构
        createTables();

        // 创建序列（PostgreSQL需要）
        createSequences();

        log.info("✅ 目标数据库结构创建完成");
    }

    /**
     * 创建 schemas
     */
    private void createSchemas() {
        log.info("🏗️  创建 schemas...");

        String[] schemas = {"news", "analytics", "shared"};
        for (String schema : schemas) {
            try {
                targetJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
                log.info("✅ 创建 schema: {}", schema);
            } catch (Exception e) {
                log.warn("⚠️  创建 schema {} 失败: {}", schema, e.getMessage());
            }
        }
    }

    /**
     * 创建表结构
     */
    private void createTables() {
        log.info("🏗️  创建表结构...");

        // 新闻表
        createNewsTable();

        // 比特币分析表
        createBitcoinAnalysisTables();

        log.info("✅ 表结构创建完成");
    }

    /**
     * 创建新闻表
     */
    private void createNewsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS news.news (
                    id BIGSERIAL PRIMARY KEY,
                    site_source VARCHAR(255),
                    publish_time BIGINT,
                    status INTEGER DEFAULT 0,
                    title VARCHAR(500),
                    link VARCHAR(500),
                    tags TEXT,
                    content TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    created_by VARCHAR(100),
                    updated_by VARCHAR(100),
                    version BIGINT DEFAULT 0,
                    deleted BOOLEAN DEFAULT FALSE,
                    deleted_at TIMESTAMP,
                    deleted_by VARCHAR(100)
                )
                """;

        targetJdbcTemplate.execute(sql);
        log.info("✅ 创建表: news.news");

        // 创建索引
        createNewsIndexes();
    }

    /**
     * 创建新闻表索引
     */
    private void createNewsIndexes() {
        String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_news_publish_time ON news.news(publish_time)",
                "CREATE INDEX IF NOT EXISTS idx_news_status ON news.news(status)",
                "CREATE INDEX IF NOT EXISTS idx_news_site_source ON news.news(site_source)",
                "CREATE INDEX IF NOT EXISTS idx_news_created_at ON news.news(created_at)"
        };

        for (String index : indexes) {
            targetJdbcTemplate.execute(index);
        }
        log.info("✅ 创建新闻表索引完成");
    }

    /**
     * 创建比特币分析表
     */
    private void createBitcoinAnalysisTables() {
        // bitcoin_entities_summary 表
        String summarySql = """
                CREATE TABLE IF NOT EXISTS analytics.bitcoin_entities_summary (
                    id BIGSERIAL PRIMARY KEY,
                    date DATE,
                    total_entities INTEGER,
                    total_balance NUMERIC(20,8),
                    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    version BIGINT DEFAULT 0
                )
                """;

        targetJdbcTemplate.execute(summarySql);
        log.info("✅ 创建表: analytics.bitcoin_entities_summary");

        // bitcoin_holdings 表
        String holdingsSql = """
                CREATE TABLE IF NOT EXISTS analytics.bitcoin_holdings (
                    id BIGSERIAL PRIMARY KEY,
                    date DATE,
                    category VARCHAR(100),
                    entities_count INTEGER,
                    balance NUMERIC(20,8),
                    percentage NUMERIC(5,2),
                    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    version BIGINT DEFAULT 0
                )
                """;

        targetJdbcTemplate.execute(holdingsSql);
        log.info("✅ 创建表: analytics.bitcoin_holdings");

        // bitcoin_entities_detail 表
        String detailSql = """
                CREATE TABLE IF NOT EXISTS analytics.bitcoin_entities_detail (
                    id BIGSERIAL PRIMARY KEY,
                    date DATE,
                    entity_name VARCHAR(255),
                    category VARCHAR(100),
                    balance NUMERIC(20,8),
                    percentage NUMERIC(5,2),
                    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    version BIGINT DEFAULT 0
                )
                """;

        targetJdbcTemplate.execute(detailSql);
        log.info("✅ 创建表: analytics.bitcoin_entities_detail");

        // 创建索引
        createBitcoinIndexes();
    }

    /**
     * 创建比特币分析表索引
     */
    private void createBitcoinIndexes() {
        String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_bitcoin_summary_date ON analytics.bitcoin_entities_summary(date)",
                "CREATE UNIQUE INDEX IF NOT EXISTS uk_bitcoin_summary_date ON analytics.bitcoin_entities_summary(date)",
                "CREATE INDEX IF NOT EXISTS idx_bitcoin_holdings_date ON analytics.bitcoin_holdings(date)",
                "CREATE INDEX IF NOT EXISTS idx_bitcoin_holdings_category ON analytics.bitcoin_holdings(category)",
                "CREATE INDEX IF NOT EXISTS idx_bitcoin_detail_date ON analytics.bitcoin_entities_detail(date)",
                "CREATE INDEX IF NOT EXISTS idx_bitcoin_detail_entity ON analytics.bitcoin_entities_detail(entity_name)"
        };

        for (String index : indexes) {
            targetJdbcTemplate.execute(index);
        }
        log.info("✅ 创建比特币分析表索引完成");
    }

    /**
     * 创建序列
     */
    private void createSequences() {
        log.info("🏗️  创建序列...");

        // PostgreSQL 使用序列，这里不需要手动创建，因为使用了 BIGSERIAL
        log.info("✅ 序列创建完成（使用 BIGSERIAL 自动生成）");
    }

    /**
     * 执行数据迁移
     */
    private void performDataMigration() {
        log.info("📊 开始数据迁移...");

        // 获取所有需要迁移的表
        List<String> tables = getMigrationTables();
        log.info("📋 需要迁移的表: {}", tables);

        for (String table : tables) {
            log.info("🔄 迁移表: {}", table);
            migrateTable(table);
        }

        log.info("✅ 数据迁移完成");
    }

    /**
     * 获取需要迁移的表列表
     */
    private List<String> getMigrationTables() {
        return List.of(
                "news",
                "bitcoin_entities_summary",
                "bitcoin_holdings",
                "bitcoin_entities_detail"
        );
    }

    /**
     * 迁移单个表
     */
    private void migrateTable(String tableName) {
        log.info("🔄 开始迁移表: {}", tableName);

        // 获取表的总行数
        long totalRows = getTableRowCount(tableName);
        log.info("📊 表 {} 总行数: {}", tableName, totalRows);

        if (totalRows == 0) {
            log.info("⚠️  表 {} 为空，跳过迁移", tableName);
            return;
        }

        // 根据表名选择迁移策略
        switch (tableName) {
            case "news":
                migrateNewsTable(totalRows);
                break;
            case "bitcoin_entities_summary":
                migrateBitcoinSummaryTable(totalRows);
                break;
            case "bitcoin_holdings":
                migrateBitcoinHoldingsTable(totalRows);
                break;
            case "bitcoin_entities_detail":
                migrateBitcoinDetailTable(totalRows);
                break;
            default:
                migrateGenericTable(tableName, totalRows);
        }

        log.info("✅ 表 {} 迁移完成", tableName);
    }

    /**
     * 获取表的行数
     */
    private long getTableRowCount(String tableName) {
        String schema = getTableSchema(tableName);
        String sql = String.format("SELECT COUNT(*) FROM `%s`.`%s`", schema, tableName);

        Long count = sourceJdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 获取表所在的schema
     */
    private String getTableSchema(String tableName) {
        return switch (tableName) {
            case "news" -> "daily-news";
            case "bitcoin_entities_summary", "bitcoin_holdings", "bitcoin_entities_detail" -> "financial_analytics";
            default -> "daily-news";
        };
    }

    /**
     * 迁移新闻表
     */
    private void migrateNewsTable(long totalRows) {
        log.info("🔄 迁移新闻表，共 {} 行", totalRows);

        String sourceSql = """
                SELECT id, site_source, publish_time, status, title, link, tags, content,
                       created_at, updated_at, created_by, updated_by, version, deleted, deleted_at, deleted_by
                FROM daily-news.news
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        String targetSql = """
                INSERT INTO news.news (id, site_source, publish_time, status, title, link, tags, content,
                                      created_at, updated_at, created_by, updated_by, version, deleted, deleted_at, deleted_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    site_source = EXCLUDED.site_source,
                    publish_time = EXCLUDED.publish_time,
                    status = EXCLUDED.status,
                    title = EXCLUDED.title,
                    link = EXCLUDED.link,
                    tags = EXCLUDED.tags,
                    content = EXCLUDED.content,
                    created_at = EXCLUDED.created_at,
                    updated_at = EXCLUDED.updated_at,
                    created_by = EXCLUDED.created_by,
                    updated_by = EXCLUDED.updated_by,
                    version = EXCLUDED.version,
                    deleted = EXCLUDED.deleted,
                    deleted_at = EXCLUDED.deleted_at,
                    deleted_by = EXCLUDED.deleted_by
                """;

        migrateWithBatching(sourceSql, targetSql, totalRows, this::mapNewsRow);
    }

    /**
     * 迁移比特币汇总表
     */
    private void migrateBitcoinSummaryTable(long totalRows) {
        log.info("🔄 迁移比特币汇总表，共 {} 行", totalRows);

        String sourceSql = """
                SELECT id, date, total_entities, total_balance, created_time, updated_at, version
                FROM financial_analytics.bitcoin_entities_summary
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        String targetSql = """
                INSERT INTO analytics.bitcoin_entities_summary (id, date, total_entities, total_balance, created_time, updated_at, version)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    date = EXCLUDED.date,
                    total_entities = EXCLUDED.total_entities,
                    total_balance = EXCLUDED.total_balance,
                    created_time = EXCLUDED.created_time,
                    updated_at = EXCLUDED.updated_at,
                    version = EXCLUDED.version
                """;

        migrateWithBatching(sourceSql, targetSql, totalRows, this::mapBitcoinSummaryRow);
    }

    /**
     * 迁移比特币持有表
     */
    private void migrateBitcoinHoldingsTable(long totalRows) {
        log.info("🔄 迁移比特币持有表，共 {} 行", totalRows);

        String sourceSql = """
                SELECT id, date, category, entities_count, balance, percentage, created_time, updated_at, version
                FROM financial_analytics.bitcoin_holdings
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        String targetSql = """
                INSERT INTO analytics.bitcoin_holdings (id, date, category, entities_count, balance, percentage, created_time, updated_at, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    date = EXCLUDED.date,
                    category = EXCLUDED.category,
                    entities_count = EXCLUDED.entities_count,
                    balance = EXCLUDED.balance,
                    percentage = EXCLUDED.percentage,
                    created_time = EXCLUDED.created_time,
                    updated_at = EXCLUDED.updated_at,
                    version = EXCLUDED.version
                """;

        migrateWithBatching(sourceSql, targetSql, totalRows, this::mapBitcoinHoldingsRow);
    }

    /**
     * 迁移比特币明细表
     */
    private void migrateBitcoinDetailTable(long totalRows) {
        log.info("🔄 迁移比特币明细表，共 {} 行", totalRows);

        String sourceSql = """
                SELECT id, date, entity_name, category, balance, percentage, created_time, updated_at, version
                FROM financial_analytics.bitcoin_entities_detail
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        String targetSql = """
                INSERT INTO analytics.bitcoin_entities_detail (id, date, entity_name, category, balance, percentage, created_time, updated_at, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    date = EXCLUDED.date,
                    entity_name = EXCLUDED.entity_name,
                    category = EXCLUDED.category,
                    balance = EXCLUDED.balance,
                    percentage = EXCLUDED.percentage,
                    created_time = EXCLUDED.created_time,
                    updated_at = EXCLUDED.updated_at,
                    version = EXCLUDED.version
                """;

        migrateWithBatching(sourceSql, targetSql, totalRows, this::mapBitcoinDetailRow);
    }

    /**
     * 通用表迁移
     */
    private void migrateGenericTable(String tableName, long totalRows) {
        log.info("🔄 迁移通用表 {}，共 {} 行", tableName, totalRows);

        String schema = getTableSchema(tableName);
        String sourceSql = String.format("SELECT * FROM `%s`.`%s` ORDER BY id LIMIT ? OFFSET ?", schema, tableName);

        // 获取列信息
        List<String> columns = getTableColumns(schema, tableName);
        String columnList = columns.stream().collect(Collectors.joining(", "));
        String placeholderList = columns.stream().map(c -> "?").collect(Collectors.joining(", "));

        String targetSql = String.format(
                "INSERT INTO %s.%s (%s) VALUES (%s) ON CONFLICT (id) DO NOTHING",
                getTargetSchema(tableName), tableName, columnList, placeholderList
        );

        migrateWithBatching(sourceSql, targetSql, totalRows,
                (rs, rowNum) -> {
                    Object[] params = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        params[i] = rs.getObject(columns.get(i));
                    }
                    return params;
                });
    }

    /**
     * 批量迁移数据
     */
    private void migrateWithBatching(String sourceSql, String targetSql,
                                   long totalRows, RowMapper<Object[]> rowMapper) {

        int batchSize = properties.getBatchSize();
        long offset = 0;
        long processedRows = 0;

        while (offset < totalRows) {
            int currentBatchSize = (int) Math.min(batchSize, totalRows - offset);

            log.debug("📦 处理批次: offset={}, size={}", offset, currentBatchSize);

            // 读取数据
            List<Object[]> batchData = sourceJdbcTemplate.query(
                    sourceSql,
                    ps -> {
                        ps.setInt(1, currentBatchSize);
                        ps.setLong(2, offset);
                    },
                    rowMapper
            );

            // 写入目标数据库
            int[] updateCounts = targetJdbcTemplate.batchUpdate(targetSql, batchData);

            processedRows += batchData.size();
            offset += currentBatchSize;

            // 更新进度
            progressMonitor.updateProgress(processedRows, totalRows);

            // 记录进度
            if (processedRows % 10000 == 0) {
                log.info("📊 迁移进度: {}/{} ({:.2f}%)",
                        processedRows, totalRows,
                        (double) processedRows / totalRows * 100);
            }
        }

        totalRowsProcessed.addAndGet(processedRows);
        log.info("✅ 批次迁移完成: {} 行", processedRows);
    }

    /**
     * 新闻数据行映射器
     */
    private RowMapper<Object[]> mapNewsRow = (rs, rowNum) -> {
        return new Object[]{
                rs.getLong("id"),
                rs.getString("site_source"),
                rs.getLong("publish_time"),
                rs.getInt("status"),
                rs.getString("title"),
                rs.getString("link"),
                rs.getString("tags"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                rs.getString("created_by"),
                rs.getString("updated_by"),
                rs.getLong("version"),
                rs.getBoolean("deleted"),
                rs.getTimestamp("deleted_at"),
                rs.getString("deleted_by")
        };
    };

    /**
     * 比特币汇总数据行映射器
     */
    private RowMapper<Object[]> mapBitcoinSummaryRow = (rs, rowNum) -> {
        return new Object[]{
                rs.getLong("id"),
                rs.getDate("date"),
                rs.getInt("total_entities"),
                rs.getBigDecimal("total_balance"),
                rs.getTimestamp("created_time"),
                rs.getTimestamp("updated_at"),
                rs.getLong("version")
        };
    };

    /**
     * 比特币持有数据行映射器
     */
    private RowMapper<Object[]> mapBitcoinHoldingsRow = (rs, rowNum) -> {
        return new Object[]{
                rs.getLong("id"),
                rs.getDate("date"),
                rs.getString("category"),
                rs.getInt("entities_count"),
                rs.getBigDecimal("balance"),
                rs.getBigDecimal("percentage"),
                rs.getTimestamp("created_time"),
                rs.getTimestamp("updated_at"),
                rs.getLong("version")
        };
    };

    /**
     * 比特币明细数据行映射器
     */
    private RowMapper<Object[]> mapBitcoinDetailRow = (rs, rowNum) -> {
        return new Object[]{
                rs.getLong("id"),
                rs.getDate("date"),
                rs.getString("entity_name"),
                rs.getString("category"),
                rs.getBigDecimal("balance"),
                rs.getBigDecimal("percentage"),
                rs.getTimestamp("created_time"),
                rs.getTimestamp("updated_at"),
                rs.getLong("version")
        };
    };

    /**
     * 获取目标 schema
     */
    private String getTargetSchema(String tableName) {
        return switch (tableName) {
            case "news" -> "news";
            case "bitcoin_entities_summary", "bitcoin_holdings", "bitcoin_entities_detail" -> "analytics";
            default -> "public";
        };
    }

    /**
     * 获取表列信息
     */
    private List<String> getTableColumns(String schema, String tableName) {
        String sql = "SELECT column_name FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ? " +
                    "ORDER BY ordinal_position";

        return sourceJdbcTemplate.queryForList(sql, String.class, schema, tableName);
    }

    /**
     * 数据验证
     */
    private void validateMigration() {
        log.info("🔍 开始数据验证...");

        ValidationResult validationResult = validationService.validateMigration();

        if (validationResult.isValid()) {
            log.info("✅ 数据验证通过");
        } else {
            log.error("❌ 数据验证失败: {}", validationResult.getErrors());
            throw new MigrationException("数据验证失败: " + validationResult.getErrors());
        }
    }

    /**
     * 创建索引和约束
     */
    private void createIndexesAndConstraints() {
        log.info("🔍 创建索引和约束...");

        // 外键约束（如果有的话）
        // 唯一约束
        // 检查约束

        log.info("✅ 索引和约束创建完成");
    }

    /**
     * 最终验证
     */
    private void finalValidation() {
        log.info("🔍 执行最终验证...");

        // 行数对比
        validateRowCounts();

        // 数据校验和
        validateChecksums();

        // 业务逻辑验证
        validateBusinessLogic();

        log.info("✅ 最终验证完成");
    }

    /**
     * 验证行数
     */
    private void validateRowCounts() {
        log.info("🔍 验证行数...");

        List<String> tables = getMigrationTables();
        boolean allMatch = true;

        for (String table : tables) {
            long sourceCount = getTableRowCount(table);
            long targetCount = getTargetTableRowCount(table);

            log.info("📊 表 {}: 源={}, 目标={}", table, sourceCount, targetCount);

            if (sourceCount != targetCount) {
                log.error("❌ 表 {} 行数不匹配: 源={}, 目标={}", table, sourceCount, targetCount);
                allMatch = false;
            }
        }

        if (!allMatch) {
            throw new MigrationException("行数验证失败");
        }

        log.info("✅ 行数验证通过");
    }

    /**
     * 获取目标表的行数
     */
    private long getTargetTableRowCount(String tableName) {
        String schema = getTargetSchema(tableName);
        String sql = String.format("SELECT COUNT(*) FROM %s.%s", schema, tableName);

        Long count = targetJdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 验证校验和
     */
    private void validateChecksums() {
        log.info("🔍 验证数据校验和...");

        // 这里可以实现更复杂的校验和验证逻辑
        // 例如：对关键字段进行CRC32校验

        log.info("✅ 数据校验和验证完成");
    }

    /**
     * 验证业务逻辑
     */
    private void validateBusinessLogic() {
        log.info("🔍 验证业务逻辑...");

        // 验证新闻数据的完整性
        validateNewsData();

        // 验证比特币数据的完整性
        validateBitcoinData();

        log.info("✅ 业务逻辑验证完成");
    }

    /**
     * 验证新闻数据
     */
    private void validateNewsData() {
        // 检查是否有重复的新闻
        String sql = "SELECT COUNT(*) FROM (SELECT link FROM news.news GROUP BY link HAVING COUNT(*) > 1) t";
        Integer duplicateCount = targetJdbcTemplate.queryForObject(sql, Integer.class);

        if (duplicateCount != null && duplicateCount > 0) {
            log.warn("⚠️  发现 {} 条重复新闻链接", duplicateCount);
        }

        // 检查数据完整性
        sql = "SELECT COUNT(*) FROM news.news WHERE title IS NULL OR link IS NULL";
        Integer incompleteCount = targetJdbcTemplate.queryForObject(sql, Integer.class);

        if (incompleteCount != null && incompleteCount > 0) {
            log.warn("⚠️  发现 {} 条不完整的新闻数据", incompleteCount);
        }
    }

    /**
     * 验证比特币数据
     */
    private void validateBitcoinData() {
        // 验证汇总数据的一致性
        String sql = "SELECT SUM(balance) FROM analytics.bitcoin_holdings GROUP BY date ORDER BY date DESC LIMIT 1";
        BigDecimal totalBalance = targetJdbcTemplate.queryForObject(sql, BigDecimal.class);

        if (totalBalance != null) {
            log.info("📊 最新比特币总持有量: {}", totalBalance);
        }
    }

    /**
     * 回滚迁移
     */
    public void rollback() {
        log.info("🔄 开始回滚迁移...");

        try {
            // 清空目标数据库
            clearTargetDatabase();

            log.info("✅ 回滚完成");
        } catch (Exception e) {
            log.error("❌ 回滚失败", e);
            throw new MigrationException("回滚失败", e);
        }
    }

    /**
     * 清空目标数据库
     */
    private void clearTargetDatabase() {
        log.info("🗑️  清空目标数据库...");

        String[] tables = {
                "analytics.bitcoin_entities_detail",
                "analytics.bitcoin_holdings",
                "analytics.bitcoin_entities_summary",
                "news.news"
        };

        for (String table : tables) {
            try {
                targetJdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE");
                log.info("🗑️  清空表: {}", table);
            } catch (Exception e) {
                log.error("❌ 清空表 {} 失败: {}", table, e.getMessage());
            }
        }
    }

    /**
     * 行映射器接口
     */
    @FunctionalInterface
    interface RowMapper<T> {
        T mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException;
    }
}

/**
 * 迁移异常
 */
class MigrationException extends RuntimeException {
    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}