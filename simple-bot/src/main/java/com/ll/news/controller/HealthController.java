package com.ll.news.controller;

import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统健康状态和基本信息
 */
@RestController
@RequestMapping("/health")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    private final TelegramBot telegramBot;
    private final UserPreferenceService userPreferenceService;

    /**
     * 基础健康检查
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "daily-news-telegram-bot");
        return result;
    }

    /**
     * 详细健康检查
     */
    @GetMapping("/detail")
    public Map<String, Object> healthDetail() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());

        // 检查Telegram连接
        Map<String, Object> telegramStatus = checkTelegramStatus();
        result.put("telegram", telegramStatus);

        // 检查数据库连接
        Map<String, Object> databaseStatus = checkDatabaseStatus();
        result.put("database", databaseStatus);

        // 系统统计
        Map<String, Object> statistics = getSystemStatistics();
        result.put("statistics", statistics);

        return result;
    }

    /**
     * 检查Telegram连接状态
     */
    private Map<String, Object> checkTelegramStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            GetMeResponse response = telegramBot.execute(new GetMe());
            if (response.isOk()) {
                User botUser = response.user();
                status.put("status", "CONNECTED");
                status.put("botName", botUser.firstName());
                status.put("botUsername", botUser.username());
                status.put("botId", botUser.id());
            } else {
                status.put("status", "ERROR");
                status.put("error", response.description());
            }
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            log.error("Telegram连接检查失败", e);
        }
        return status;
    }

    /**
     * 检查数据库连接状态
     */
    private Map<String, Object> checkDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // 简单的数据库查询测试
            long count = userPreferenceService.getAllUsers().size();
            status.put("status", "CONNECTED");
            status.put("userCount", count);
            status.put("connectionTest", "PASSED");
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            log.error("数据库连接检查失败", e);
        }
        return status;
    }

    /**
     * 获取系统统计信息
     */
    private Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("activeUsers", userPreferenceService.getActiveUsers().size());
            stats.put("totalUsers", userPreferenceService.getAllUsers().size());
            stats.put("totalSubscriptions", userPreferenceService.getTotalSubscriptions());
            stats.put("uptime", System.currentTimeMillis());
        } catch (Exception e) {
            stats.put("error", "统计信息获取失败: " + e.getMessage());
            log.error("系统统计获取失败", e);
        }
        return stats;
    }

    /**
     * 就绪检查
     */
    @GetMapping("/ready")
    public Map<String, Object> ready() {
        Map<String, Object> result = new HashMap<>();

        // 检查必要组件是否就绪
        boolean telegramReady = isTelegramReady();
        boolean databaseReady = isDatabaseReady();

        result.put("status", telegramReady && databaseReady ? "READY" : "NOT_READY");
        result.put("telegram", telegramReady);
        result.put("database", databaseReady);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    private boolean isTelegramReady() {
        try {
            GetMeResponse response = telegramBot.execute(new GetMe());
            return response.isOk();
        } catch (Exception e) {
            log.error("Telegram就绪检查失败", e);
            return false;
        }
    }

    private boolean isDatabaseReady() {
        try {
            userPreferenceService.getAllUsers();
            return true;
        } catch (Exception e) {
            log.error("数据库就绪检查失败", e);
            return false;
        }
    }
}