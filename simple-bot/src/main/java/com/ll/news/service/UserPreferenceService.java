package com.ll.news.service;

import com.ll.news.entity.UserPreference;
import com.ll.news.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户偏好服务 - 使用Spring Data JPA
 * 遵循Linus的简洁原则：只做必要的事，做好必要的事
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * 初始化用户偏好
     */
    @Transactional
    public void initializeUser(Long userId) {
        if (userPreferenceRepository.existsByUserId(userId)) {
            log.debug("用户{}已存在，跳过初始化", userId);
            return;
        }

        UserPreference preference = UserPreference.builder()
                .userId(userId)
                .keywords("")
                .pushFrequency(30)
                .enabled(true)
                .pushCount(0)
                .build();

        userPreferenceRepository.save(preference);
        log.info("初始化用户{}的偏好设置成功", userId);
    }

    /**
     * 获取用户偏好
     */
    public Optional<UserPreference> getUserPreference(Long userId) {
        return userPreferenceRepository.findById(userId);
    }

    /**
     * 更新用户偏好
     */
    @Transactional
    public void updateUserPreference(UserPreference preference) {
        userPreferenceRepository.save(preference);
        log.debug("更新用户{}的偏好设置", preference.getUserId());
    }

    /**
     * 添加关键词
     */
    @Transactional
    public boolean addKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试添加空关键词，用户：{}", userId);
            return false;
        }

        Optional<UserPreference> optional = getUserPreference(userId);
        if (optional.isEmpty()) {
            log.warn("用户{}不存在，无法添加关键词", userId);
            return false;
        }

        UserPreference preference = optional.get();
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
    @Transactional
    public boolean removeKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试移除空关键词，用户：{}", userId);
            return false;
        }

        Optional<UserPreference> optional = getUserPreference(userId);
        if (optional.isEmpty()) {
            log.warn("用户{}不存在，无法移除关键词", userId);
            return false;
        }

        UserPreference preference = optional.get();
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
        return getUserPreference(userId)
                .map(pref -> pref.getKeywordList().length)
                .orElse(0);
    }

    /**
     * 获取所有活跃用户
     */
    public List<UserPreference> getActiveUsers() {
        return userPreferenceRepository.findByEnabledTrue();
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsByUserId(Long userId) {
        return userPreferenceRepository.existsByUserId(userId);
    }

    /**
     * 获取所有用户（管理用）
     */
    public List<UserPreference> getAllUsers() {
        return userPreferenceRepository.findAll();
    }

    /**
     * 统计订阅总数
     */
    public int getTotalSubscriptions() {
        return userPreferenceRepository.findAll().stream()
                .mapToInt(pref -> pref.getKeywordList().length)
                .sum();
    }
}