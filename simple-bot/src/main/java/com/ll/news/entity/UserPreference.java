package com.ll.news.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用户偏好实体 - PostgreSQL持久化
 * 遵循Linus的简洁原则：只做一件事，做好一件事
 */
@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;  // 逗号分隔的关键词

    @Column(name = "push_frequency", nullable = false)
    @Builder.Default
    private Integer pushFrequency = 30;  // 推送频率（分钟）

    @Column(name = "push_start_time", nullable = false)
    @Builder.Default
    private LocalTime pushStartTime = LocalTime.of(9, 0);

    @Column(name = "push_end_time", nullable = false)
    @Builder.Default
    private LocalTime pushEndTime = LocalTime.of(22, 0);

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_push_at")
    private LocalDateTime lastPushAt;

    @Column(name = "push_count", nullable = false)
    @Builder.Default
    private Integer pushCount = 0;

    /**
     * 获取关键词列表
     */
    public String[] getKeywordList() {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new String[0];
        }
        return keywords.split(",");
    }

    /**
     * 添加关键词
     */
    public boolean addKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        keyword = keyword.trim();
        String[] existing = getKeywordList();

        // 检查是否已存在
        for (String k : existing) {
            if (k.trim().equalsIgnoreCase(keyword)) {
                return false;
            }
        }

        // 检查数量限制
        if (existing.length >= 10) {
            return false;
        }

        // 添加关键词
        if (keywords == null || keywords.trim().isEmpty()) {
            keywords = keyword;
        } else {
            keywords = keywords + "," + keyword;
        }
        return true;
    }

    /**
     * 移除关键词
     */
    public boolean removeKeyword(String keyword) {
        if (keyword == null || keywords == null || keywords.trim().isEmpty()) {
            return false;
        }

        String[] existing = getKeywordList();
        if (existing.length == 0) {
            return false;
        }

        StringBuilder newKeywords = new StringBuilder();
        boolean found = false;

        for (String k : existing) {
            if (!k.trim().equalsIgnoreCase(keyword.trim())) {
                if (newKeywords.length() > 0) {
                    newKeywords.append(",");
                }
                newKeywords.append(k.trim());
            } else {
                found = true;
            }
        }

        if (found) {
            keywords = newKeywords.toString();
            return true;
        }
        return false;
    }

    /**
     * 记录推送
     */
    public void recordPush() {
        this.lastPushAt = LocalDateTime.now();
        this.pushCount++;
    }

    /**
     * 检查是否应该推送
     */
    public boolean shouldPush() {
        if (!enabled) {
            return false;
        }
        if (lastPushAt == null) {
            return true;
        }
        LocalDateTime nextPush = lastPushAt.plusMinutes(pushFrequency);
        return LocalDateTime.now().isAfter(nextPush);
    }

    /**
     * 检查是否在推送时间窗口内
     */
    public boolean isInPushWindow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(pushStartTime) && !now.isAfter(pushEndTime);
    }
}