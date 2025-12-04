package com.ll.news.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用户偏好设置实体
 * 用于存储Telegram用户的个性化订阅和推送偏好
 */
@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;  // Telegram用户ID

    @Column(name = "keywords", length = 1000)
    private String keywords;  // 订阅关键词，逗号分隔

    @Column(name = "push_frequency", nullable = false)
    @Builder.Default
    private Integer pushFrequency = 30;  // 推送频率（分钟）

    @Column(name = "push_start_time")
    @Builder.Default
    private LocalTime pushStartTime = LocalTime.of(9, 0);  // 推送开始时间

    @Column(name = "push_end_time")
    @Builder.Default
    private LocalTime pushEndTime = LocalTime.of(22, 0);  // 推送结束时间

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // 是否启用推送

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_push_at")
    private LocalDateTime lastPushAt;  // 上次推送时间

    @Column(name = "push_count", nullable = false)
    @Builder.Default
    private Integer pushCount = 0;  // 推送次数统计

    /**
     * 获取关键词列表
     */
    public List<String> getKeywordList() {
        if (keywords == null || keywords.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(keywords.split(","));
    }

    /**
     * 设置关键词列表
     */
    public void setKeywordList(List<String> keywordList) {
        if (keywordList == null || keywordList.isEmpty()) {
            this.keywords = null;
        } else {
            this.keywords = String.join(",", keywordList);
        }
    }

    /**
     * 添加关键词
     */
    public boolean addKeyword(String keyword) {
        List<String> keywords = getKeywordList();
        if (keywords.contains(keyword)) {
            return false;  // 已存在
        }
        keywords.add(keyword);
        setKeywordList(keywords);
        return true;
    }

    /**
     * 移除关键词
     */
    public boolean removeKeyword(String keyword) {
        List<String> keywords = getKeywordList();
        boolean removed = keywords.remove(keyword);
        if (removed) {
            setKeywordList(keywords);
        }
        return removed;
    }

    /**
     * 检查是否在推送时间窗口内
     */
    public boolean isInPushWindow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(pushStartTime) && !now.isAfter(pushEndTime);
    }

    /**
     * 检查是否需要推送（基于频率控制）
     */
    public boolean shouldPush() {
        if (!enabled) {
            return false;
        }

        if (lastPushAt == null) {
            return true;  // 从未推送过
        }

        LocalDateTime nextPushTime = lastPushAt.plusMinutes(pushFrequency);
        return LocalDateTime.now().isAfter(nextPushTime) && isInPushWindow();
    }

    /**
     * 记录推送
     */
    public void recordPush() {
        this.lastPushAt = LocalDateTime.now();
        this.pushCount++;
    }
}