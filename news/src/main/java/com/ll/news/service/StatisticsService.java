package com.ll.news.service;

/**
 * 统计服务接口
 * 提供系统统计信息
 */
public interface StatisticsService {

    /**
     * 获取今日新闻抓取数量
     */
    int getTodayNewsCount();

    /**
     * 获取今日推送数量
     */
    int getTodayPushCount();

    /**
     * 获取活跃数据源数量
     */
    int getActiveSources();

    /**
     * 获取活跃用户数量
     */
    int getActiveUsers();

    /**
     * 获取总订阅数量
     */
    int getTotalSubscriptions();

    /**
     * 获取系统运行时间
     */
    String getUptime();

    /**
     * 获取系统统计DTO
     */
    StatisticsDTO getStatistics();
}