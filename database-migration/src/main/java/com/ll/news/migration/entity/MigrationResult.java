package com.ll.news.migration.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 迁移结果实体
 * 记录数据库迁移的详细结果
 */
@Data
@Builder
public class MigrationResult {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double duration; // 秒
    private MigrationStatus status;
    private String errorMessage;
    private long totalRowsProcessed;
    private long totalRowsFailed;
    private int tablesProcessed;
    private int tablesFailed;

    /**
     * 获取迁移摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("迁移结果: ").append(status).append("\n");
        summary.append("开始时间: ").append(startTime).append("\n");
        summary.append("结束时间: ").append(endTime).append("\n");
        summary.append("总耗时: ").append(String.format("%.2f", duration)).append(" 秒\n");
        summary.append("处理行数: ").append(totalRowsProcessed).append("\n");
        summary.append("失败行数: ").append(totalRowsFailed).append("\n");
        summary.append("处理表数: ").append(tablesProcessed).append("\n");
        summary.append("失败表数: ").append(tablesFailed).append("\n");

        if (errorMessage != null) {
            summary.append("错误信息: ").append(errorMessage).append("\n");
        }

        return summary.toString();
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return status == MigrationStatus.SUCCESS;
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return status == MigrationStatus.FAILED;
    }

    /**
     * 获取处理速度（行/秒）
     */
    public double getProcessingSpeed() {
        return duration > 0 ? (double) totalRowsProcessed / duration : 0;
    }
}

/**
 * 迁移状态枚举
 */
enum MigrationStatus {
    RUNNING,     // 运行中
    SUCCESS,     // 成功
    FAILED,      // 失败
    ROLLED_BACK  // 已回滚
}