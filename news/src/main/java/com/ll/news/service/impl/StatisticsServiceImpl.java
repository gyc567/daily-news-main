package com.ll.news.service.impl;

import com.ll.news.service.StatisticsDTO;
import com.ll.news.service.StatisticsService;
import com.ll.news.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统计服务实现类
 * 提供系统统计信息的简化实现
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserPreferenceService userPreferenceService;

    // 模拟应用启动时间（实际项目中应该从应用启动时记录）
    private static final LocalDateTime APP_START_TIME = LocalDateTime.now().minusDays(1);

    @Override
    public int getTodayNewsCount() {
        // 模拟今日新闻数量，实际项目中应该从数据库查询
        return 127;
    }

    @Override
    public int getTodayPushCount() {
        // 模拟今日推送数量
        return 89;
    }

    @Override
    public int getActiveSources() {
        // 模拟活跃数据源数量
        return 5;
    }

    @Override
    public int getActiveUsers() {
        // 从用户偏好服务获取活跃用户数量
        try {
            return (int) userPreferenceService.getActiveUserCount();
        } catch (Exception e) {
            log.warn("获取活跃用户数量失败，使用默认值", e);
            return 12; // 默认值
        }
    }

    @Override
    public int getTotalSubscriptions() {
        // 从用户偏好服务获取总订阅数量
        try {
            return (int) userPreferenceService.getTotalSubscriptionCount();
        } catch (Exception e) {
            log.warn("获取总订阅数量失败，使用默认值", e);
            return 28; // 默认值
        }
    }

    @Override
    public String getUptime() {
        Duration uptime = Duration.between(APP_START_TIME, LocalDateTime.now());
        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    @Override
    public StatisticsDTO getStatistics() {
        return StatisticsDTO.builder()
                .todayNewsCount(getTodayNewsCount())
                .todayPushCount(getTodayPushCount())
                .activeSources(getActiveSources())
                .activeUsers(getActiveUsers())
                .totalSubscriptions(getTotalSubscriptions())
                .uptime(getUptime())
                .build();
    }
}