package com.ll.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ll.news.entity.UserPreference;
import com.ll.news.mapper.UserPreferenceMapper;
import com.ll.news.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户偏好服务实现类
 */
@Service
@Slf4j
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference>
        implements UserPreferenceService {

    private static final int MAX_KEYWORDS = 10;  // 最大关键词数量

    @Override
    @Transactional
    public void initializeUser(Long userId) {
        if (existsByUserId(userId)) {
            log.debug("用户{}已存在，跳过初始化", userId);
            return;
        }

        UserPreference preference = UserPreference.builder()
                .userId(userId)
                .keywords("")
                .pushFrequency(30)
                .pushStartTime(LocalTime.of(9, 0))
                .pushEndTime(LocalTime.of(22, 0))
                .enabled(true)
                .pushCount(0)
                .build();

        save(preference);
        log.info("初始化用户{}的偏好设置成功", userId);
    }

    @Override
    public UserPreference getUserPreference(Long userId) {
        return getById(userId);
    }

    @Override
    @Transactional
    public void updateUserPreference(UserPreference preference) {
        updateById(preference);
        log.debug("更新用户{}的偏好设置", preference.getUserId());
    }

    @Override
    @Transactional
    public boolean addKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试添加空关键词，用户：{}", userId);
            return false;
        }

        keyword = keyword.trim();
        UserPreference preference = getUserPreference(userId);

        if (preference == null) {
            log.warn("用户{}不存在，无法添加关键词", userId);
            return false;
        }

        // 检查关键词数量限制
        List<String> keywords = preference.getKeywordList();
        if (keywords.size() >= MAX_KEYWORDS) {
            log.warn("用户{}关键词数量已达上限{}，无法添加更多", userId, MAX_KEYWORDS);
            return false;
        }

        boolean added = preference.addKeyword(keyword);
        if (added) {
            updateUserPreference(preference);
            log.info("用户{}添加关键词\"{}\"成功", userId, keyword);
        } else {
            log.warn("用户{}关键词\"{}\"已存在", userId, keyword);
        }

        return added;
    }

    @Override
    @Transactional
    public boolean removeKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("尝试移除空关键词，用户：{}", userId);
            return false;
        }

        keyword = keyword.trim();
        UserPreference preference = getUserPreference(userId);

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

    @Override
    public int getKeywordCount(Long userId) {
        UserPreference preference = getUserPreference(userId);
        return preference != null ? preference.getKeywordList().size() : 0;
    }

    @Override
    public List<UserPreference> getActiveUsers() {
        QueryWrapper<UserPreference> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_enabled", true);
        return list(queryWrapper);
    }

    @Override
    public List<UserPreference> getAllUserPreferences() {
        return list();
    }

    @Override
    @Transactional
    public boolean updatePushSettings(Long userId, Integer frequency, LocalTime startTime, LocalTime endTime) {
        UserPreference preference = getUserPreference(userId);
        if (preference == null) {
            log.warn("用户{}不存在，无法更新推送设置", userId);
            return false;
        }

        preference.setPushFrequency(frequency);
        preference.setPushStartTime(startTime);
        preference.setPushEndTime(endTime);

        updateUserPreference(preference);
        log.info("用户{}更新推送设置：频率{}分钟，时间{}-{}",
                userId, frequency, startTime, endTime);
        return true;
    }

    @Override
    @Transactional
    public boolean setPushEnabled(Long userId, boolean enabled) {
        UserPreference preference = getUserPreference(userId);
        if (preference == null) {
            log.warn("用户{}不存在，无法设置推送状态", userId);
            return false;
        }

        preference.setEnabled(enabled);
        updateUserPreference(preference);
        log.info("用户{}设置推送状态为：{}", userId, enabled ? "启用" : "禁用");
        return true;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return getById(userId) != null;
    }

    @Override
    @Transactional
    public void deleteUserPreference(Long userId) {
        removeById(userId);
        log.info("删除用户{}的偏好设置", userId);
    }

    @Override
    public long getUserCount() {
        return count();
    }

    @Override
    public long getActiveUserCount() {
        QueryWrapper<UserPreference> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_enabled", true);
        return count(queryWrapper);
    }

    @Override
    public long getTotalSubscriptionCount() {
        List<UserPreference> allUsers = list();
        return allUsers.stream()
                .mapToLong(user -> user.getKeywordList().size())
                .sum();
    }
}