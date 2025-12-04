package com.ll.news.service;

import com.ll.news.entity.UserPreference;

import java.time.LocalTime;
import java.util.List;

/**
 * 用户偏好服务接口
 * 管理Telegram用户的个性化订阅和推送偏好
 */
public interface UserPreferenceService {

    /**
     * 初始化用户偏好
     * 为新用户创建默认偏好设置
     */
    void initializeUser(Long userId);

    /**
     * 获取用户偏好
     */
    UserPreference getUserPreference(Long userId);

    /**
     * 更新用户偏好
     */
    void updateUserPreference(UserPreference preference);

    /**
     * 添加关键词
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 是否添加成功
     */
    boolean addKeyword(Long userId, String keyword);

    /**
     * 移除关键词
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 是否移除成功
     */
    boolean removeKeyword(Long userId, String keyword);

    /**
     * 获取关键词数量
     */
    int getKeywordCount(Long userId);

    /**
     * 获取活跃用户列表（启用了推送的用户）
     */
    List<UserPreference> getActiveUsers();

    /**
     * 获取所有用户偏好
     */
    List<UserPreference> getAllUserPreferences();

    /**
     * 更新推送设置
     * @param userId 用户ID
     * @param frequency 推送频率（分钟）
     * @param startTime 推送开始时间
     * @param endTime 推送结束时间
     * @return 是否更新成功
     */
    boolean updatePushSettings(Long userId, Integer frequency, LocalTime startTime, LocalTime endTime);

    /**
     * 启用/禁用推送
     * @param userId 用户ID
     * @param enabled 是否启用
     * @return 是否更新成功
     */
    boolean setPushEnabled(Long userId, boolean enabled);

    /**
     * 检查用户是否存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 删除用户偏好
     */
    void deleteUserPreference(Long userId);

    /**
     * 获取用户统计信息
     */
    long getUserCount();

    /**
     * 获取活跃用户统计
     */
    long getActiveUserCount();

    /**
     * 获取总订阅数统计
     */
    long getTotalSubscriptionCount();
}