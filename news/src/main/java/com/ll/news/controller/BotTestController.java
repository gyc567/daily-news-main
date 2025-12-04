package com.ll.news.controller;

import com.ll.news.bot.TelegramBotService;
import com.ll.news.service.SimpleUserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器人测试控制器
 * 用于测试Telegram机器人功能
 */
@RestController
@RequestMapping("/api/bot")
@Slf4j
public class BotTestController {

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private SimpleUserPreferenceService userPreferenceService;

    /**
     * 发送测试消息
     */
    @PostMapping("/test-message")
    public Map<String, Object> sendTestMessage(@RequestParam(required = false, defaultValue = "8291537816") Long chatId,
                                               @RequestParam(required = false, defaultValue = "测试消息") String message) {
        log.info("发送测试消息到用户: {}", chatId);

        boolean success = telegramBotService.sendMessage(chatId, message);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("chatId", chatId);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());

        if (success) {
            result.put("status", "消息发送成功");
        } else {
            result.put("status", "消息发送失败");
        }

        return result;
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        log.info("获取用户信息: {}", userId);

        var preference = userPreferenceService.getUserPreference(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("exists", preference != null);

        if (preference != null) {
            result.put("enabled", preference.getEnabled());
            result.put("keywords", preference.getKeywordList());
            result.put("keywordCount", preference.getKeywordList().size());
            result.put("pushFrequency", preference.getPushFrequency());
            result.put("pushStartTime", preference.getPushStartTime().toString());
            result.put("pushEndTime", preference.getPushEndTime().toString());
            result.put("pushCount", preference.getPushCount());
            result.put("lastPushAt", preference.getLastPushAt());
        }

        return result;
    }

    /**
     * 初始化用户
     */
    @PostMapping("/user/{userId}/init")
    public Map<String, Object> initializeUser(@PathVariable Long userId) {
        log.info("初始化用户: {}", userId);

        userPreferenceService.initializeUser(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("status", "用户初始化成功");

        return result;
    }

    /**
     * 获取系统统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getSystemStats() {
        log.info("获取系统统计信息");

        var activeUsers = userPreferenceService.getActiveUsers();
        int totalSubscriptions = 0;
        for (var user : activeUsers) {
            totalSubscriptions += user.getKeywordList().size();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("activeUsers", activeUsers.size());
        result.put("totalSubscriptions", totalSubscriptions);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 检查机器人状态
     */
    @GetMapping("/status")
    public Map<String, Object> getBotStatus() {
        log.info("检查机器人状态");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "running");
        result.put("botToken", "8291537816:AAEQTE7Jd5AGQ9dkq7NMPewlSr8Kun2qXao");
        result.put("timestamp", System.currentTimeMillis());
        result.put("version", "1.0.0");
        result.put("features", new String[]{
            "个性化订阅",
            "关键词管理",
            "推送设置",
            "用户偏好"
        });

        return result;
    }
}