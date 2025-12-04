package com.ll.news.listener;

import com.ll.news.common.NewsEvent;
import com.ll.news.bot.TelegramBotService;
import com.ll.news.entity.News;
import com.ll.news.entity.UserPreference;
import com.ll.news.enumeration.NewsStatus;
import com.ll.news.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 个性化新闻监听器
 * 根据用户偏好推送个性化新闻
 */
@Component
@Slf4j
public class PersonalizedNewsListener {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private TelegramBotService telegramBotService;

    @EventListener(classes = NewsEvent.class)
    @Async("personalizedExecutor")  // 使用专用线程池
    public void handlePersonalizedNews(NewsEvent event) {
        News news = event.getNews();

        // 只在新闻状态为"已发布"时处理
        if (news.getStatus() != NewsStatus.PUBLISHED.getCode()) {
            return;
        }

        log.info("处理个性化新闻推送: {}", news.getTitle());

        // 获取活跃用户列表
        List<UserPreference> activeUsers = userPreferenceService.getActiveUsers();

        int pushedCount = 0;
        for (UserPreference user : activeUsers) {
            if (shouldPushToUser(news, user)) {
                try {
                    sendPersonalizedNews(news, user);
                    user.recordPush();  // 记录推送
                    userPreferenceService.updateUserPreference(user);  // 更新推送记录
                    pushedCount++;
                } catch (Exception e) {
                    log.error("个性化推送给用户{}失败", user.getUserId(), e);
                }
            }
        }

        log.info("个性化新闻推送完成，共推送给{}位用户", pushedCount);
    }

    private boolean shouldPushToUser(News news, UserPreference user) {
        // 1. 检查是否启用推送
        if (!user.getEnabled()) {
            return false;
        }

        // 2. 检查时间窗口
        if (!user.isInPushWindow()) {
            return false;
        }

        // 3. 检查频率控制
        if (!user.shouldPush()) {
            return false;
        }

        // 4. 关键词匹配
        return matchesKeywords(news, user.getKeywordList());
    }

    private boolean matchesKeywords(News news, List<String> keywords) {
        if (keywords.isEmpty()) {
            return false;  // 没有订阅关键词，不匹配
        }

        String content = (news.getTitle() + " " + news.getContent()).toLowerCase();

        return keywords.stream()
                .anyMatch(keyword -> content.contains(keyword.toLowerCase()));
    }

    private void sendPersonalizedNews(News news, UserPreference user) {
        // 构建个性化消息
        String message = buildPersonalizedMessage(news, user);

        // 发送消息到用户
        boolean sent = telegramBotService.sendMessage(user.getUserId(), message);
        if (sent) {
            log.info("向用户{}发送个性化新闻成功", user.getUserId());
        } else {
            log.error("向用户{}发送个性化新闻失败", user.getUserId());
        }
    }

    private String buildPersonalizedMessage(News news, UserPreference user) {
        String keywords = user.getKeywords();
        String matchedKeyword = findMatchedKeyword(news, user.getKeywordList());

        return String.format("🎯 为您推送个性化新闻\n\n" +
                "📰 %s\n" +
                "【%s】%s\n\n" +
                "🔗 %s\n" +
                "⏰ %s\n" +
                "%s\n\n" +
                "💡 基于您的订阅：%s",
                matchedKeyword != null ? "🏷️ 匹配关键词：" + matchedKeyword : "",
                news.getSiteSource(),
                truncate(news.getTitle(), 100),
                news.getLink(),
                formatTime(news.getPublishTime()),
                truncate(news.getContent(), 200),
                truncate(keywords, 50)
        );
    }

    private String findMatchedKeyword(News news, List<String> keywords) {
        String content = (news.getTitle() + " " + news.getContent()).toLowerCase();

        return keywords.stream()
                .filter(keyword -> content.contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    private String formatTime(Long timestamp) {
        if (timestamp == null) {
            return "未知";
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                           .format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}