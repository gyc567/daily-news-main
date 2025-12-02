package com.ll.news.migration.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果实体
 * 记录数据验证的详细结果
 */
@Data
@Builder
public class ValidationResult {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    // 验证结果详情
    private boolean rowCountValidationPassed;
    private boolean dataTypeValidationPassed;
    private boolean constraintValidationPassed;
    private boolean dataIntegrityValidationPassed;
    private boolean businessLogicValidationPassed;
    private boolean checksumValidationPassed;
    private boolean sampleValidationPassed;

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 获取验证摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("验证结果: ").append(valid ? "通过" : "失败").append("\n");
        summary.append("验证时间: ").append(startTime).append(" - ").append(endTime).append("\n");

        if (hasErrors()) {
            summary.append("错误数: ").append(errors.size()).append("\n");
            summary.append("错误详情:\n");
            errors.forEach(error -> summary.append("  - ").append(error).append("\n"));
        }

        if (hasWarnings()) {
            summary.append("警告数: ").append(warnings.size()).append("\n");
            summary.append("警告详情:\n");
            warnings.forEach(warning -> summary.append("  - ").append(warning).append("\n"));
        }

        summary.append("详细验证结果:\n");
        summary.append("  - 行数验证: ").append(rowCountValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 数据类型验证: ").append(dataTypeValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 约束验证: ").append(constraintValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 数据完整性验证: ").append(dataIntegrityValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 业务逻辑验证: ").append(businessLogicValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 校验和验证: ").append(checksumValidationPassed ? "通过" : "失败").append("\n");
        summary.append("  - 抽样验证: ").append(sampleValidationPassed ? "通过" : "失败").append("\n");

        return summary.toString();
    }
}