package com.ll.news.service;

import com.ll.news.entity.SimpleUserPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化的用户偏好服务
 * 使用内存存储，不依赖数据库
 */
@Service
@Slf4j
public class SimpleUserPreferenceService {

    private final Map<Long, SimpleUserPreference> userPreferences = new ConcurrentHashMap<>();

    /**
     * 初始化用户偏好
     */
    public void initializeUser(Long userId) {
        if (userPreferences.containsKey(userId)) {
            log.debug("用户{}已存在，跳过初始化", userId);
            return;
        }

        SimpleUserPreference preference = new SimpleUserPreference(userId);
        userPreferences.put(userId, preference);
        log.info("初始化用户{}的偏好设置成功", userId);
    }

    /**
     * 获取用户偏好
     */
    public SimpleUserPreference getUserPreference(Long userId) {
        return userPreferences.get(userId);
    }

    /**
     * 更新用户偏好
     */
    public void updateUserPreference(SimpleUserPreference preference) {
        if (preference != null && preference.getUserId() != null) {
            userPreferences.put(preference.getUserId(), preference);
            log.debug("更新用户{}的偏好设置", preference.getUserId());
        }
    }

    /**
     * 添加关键词
     */
    public boolean addKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试添加空关键词，用户：{}", userId);
            return false;
        }

        SimpleUserPreference preference = userPreferences.get(userId);
        if (preference == null) {
            log.warn("用户{}不存在，无法添加关键词", userId);
            return false;
        }

        boolean added = preference.addKeyword(keyword);
        if (added) {
            updateUserPreference(preference);
            log.info("用户{}添加关键词\"{}\"成功", userId, keyword);
        } else {
            log.warn("用户{}关键词\"{}\"已存在或达到上限", userId, keyword);
        }

        return added;
    }

    /**
     * 移除关键词
     */
    public boolean removeKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试移除空关键词，用户：{}", userId);
            return false;
        }

        SimpleUserPreference preference = userPreferences.get(userId);
        if (preference == null) {
            log.warn("用户{}不存在，无法移除关键词", userId);
            return false;
        }

        boolean removed = preference.removeKeyword(keyword);
        if (removed) {
            updateUserPreference(preference);
            log.info("用户{}移除关键词\"{}\"成功", userId, keyword);
        } else {
            log.warn("用户{}关键词\"{}\"不存在", userId, keyword);
        }

        return removed;
    }

    /**
     * 获取关键词数量
     */
    public int getKeywordCount(Long userId) {
        SimpleUserPreference preference = userPreferences.get(userId);
        return preference != null ? preference.getKeywordList().size() : 0;
    }

    /**
     * 获取所有活跃用户
     */
    public List<SimpleUserPreference> getActiveUsers() {
        List<SimpleUserPreference> activeUsers = new ArrayList<>();
        for (SimpleUserPreference preference : userPreferences.values()) {
            if (preference.getEnabled()) {
                activeUsers.add(preference);
            }
        }
        return activeUsers;
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsByUserId(Long userId) {
        return userPreferences.containsKey(userId);
    }
}