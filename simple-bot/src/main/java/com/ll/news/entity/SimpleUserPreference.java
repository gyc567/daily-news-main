package com.ll.news.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化的用户偏好实体
 * 使用内存存储，不依赖数据库
 */
@Data
@NoArgsConstructor
public class SimpleUserPreference {
    private Long userId;
    private List<String> keywords = new ArrayList<>();
    private Integer pushFrequency = 30; // 推送频率（分钟）
    private LocalTime pushStartTime = LocalTime.of(9, 0);
    private LocalTime pushEndTime = LocalTime.of(22, 0);
    private Boolean enabled = true;
    private LocalDateTime lastPushAt;
    private Integer pushCount = 0;

    public SimpleUserPreference(Long userId) {
        this.userId = userId;
    }

    public boolean addKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        keyword = keyword.trim();
        if (keywords.contains(keyword)) {
            return false;
        }
        if (keywords.size() >= 10) { // 最多10个关键词
            return false;
        }
        return keywords.add(keyword);
    }

    public boolean removeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        return keywords.remove(keyword.trim());
    }

    public List<String> getKeywordList() {
        return new ArrayList<>(keywords);
    }

    public String getKeywords() {
        return String.join(",", keywords);
    }

    public void setKeywords(String keywords) {
        this.keywords.clear();
        if (keywords != null && !keywords.trim().isEmpty()) {
            String[] parts = keywords.split(",");
            for (String part : parts) {
                String keyword = part.trim();
                if (!keyword.isEmpty()) {
                    this.keywords.add(keyword);
                }
            }
        }
    }

    public void recordPush() {
        this.lastPushAt = LocalDateTime.now();
        this.pushCount++;
    }

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

    public boolean isInPushWindow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(pushStartTime) && !now.isAfter(pushEndTime);
    }
}