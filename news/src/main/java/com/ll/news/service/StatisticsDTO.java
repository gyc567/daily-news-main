package com.ll.news.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private int todayNewsCount;
    private int todayPushCount;
    private int activeSources;
    private int activeUsers;
    private int totalSubscriptions;
    private String uptime;
}