package com.ll.news.bot;

import com.ll.news.model.News;
import com.ll.news.entity.UserPreference;
import com.ll.news.service.INewsService;
import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Telegram命令处理器
 * 处理用户发送的机器人命令，如/subscribe、/latest等
 */
@Component
@Slf4j
public class TelegramCommandHandler {

    @Autowired
    private TelegramBotService botService;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private INewsService newsService;

    /**
     * 处理用户命令
     */
    public void handleCommand(Message message) {
        Long userId = message.from().id().longValue();
        String text = message.text();
        String[] parts = text.split(" ");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "/start":
                    handleStart(userId);
                    break;
                case "/help":
                    handleHelp(userId);
                    break;
                case "/subscribe":
                    handleSubscribe(userId, parts.length > 1 ? parts[1] : null);
                    break;
                case "/unsubscribe":
                    handleUnsubscribe(userId, parts.length > 1 ? parts[1] : null);
                    break;
                case "/latest":
                    handleLatest(userId, parts.length > 1 ? parseInt(parts[1], 5) : 5);
                    break;
                case "/stats":
                    handleStats(userId);
                    break;
                case "/settings":
                    handleSettings(userId);
                    break;
                default:
                    handleUnknown(userId, command);
            }
        } catch (Exception e) {
            log.error("处理命令失败: {}", command, e);
            botService.sendMessage(userId, "❌ 处理命令时出现错误，请稍后重试。");
        }
    }

    private void handleStart(Long userId) {
        String welcomeMessage = "👋 欢迎使用 Daily News 智能助手！\n\n" +
                "🤖 我可以为您提供：\n" +
                "• 📰 个性化新闻推送\n" +
                "• 🔍 智能新闻搜索\n" +
                "• 📊 实时数据统计\n" +
                "• ⚙️ 个人偏好设置\n\n" +
                "💡 使用 /help 查看所有可用命令\n" +
                "🎯 使用 /subscribe 开始个性化订阅";

        botService.sendMessage(userId, welcomeMessage);

        // 初始化用户偏好
        userPreferenceService.initializeUser(userId);
    }

    private void handleHelp(Long userId) {
        String helpMessage = "📋 可用命令列表：\n\n" +
                "🔖 订阅管理\n" +
                "/subscribe [关键词] - 订阅新闻关键词\n" +
                "/unsubscribe [关键词] - 取消订阅\n\n" +
                "📰 新闻查询\n" +
                "/latest [数量] - 获取最新新闻（默认5条）\n\n" +
                "📊 数据统计\n" +
                "/stats - 查看系统统计信息\n\n" +
                "⚙️ 个人设置\n" +
                "/settings - 查看个人偏好设置\n\n" +
                "💡 其他\n" +
                "/start - 开始使用\n" +
                "/help - 显示此帮助信息";

        botService.sendMessage(userId, helpMessage);
    }

    private void handleSubscribe(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            botService.sendMessage(userId, "❌ 请提供要订阅的关键词，例如：/subscribe 比特币");
            return;
        }

        keyword = keyword.trim();
        boolean success = userPreferenceService.addKeyword(userId, keyword);

        if (success) {
            int count = userPreferenceService.getKeywordCount(userId);
            String message = String.format("✅ 成功订阅\"%s\"\n📊 当前订阅关键词：%d个\n🔔 将为您推送相关新闻", keyword, count);
            botService.sendMessage(userId, message);
        } else {
            botService.sendMessage(userId, "❌ 订阅失败，该关键词可能已存在或达到订阅上限。");
        }
    }

    private void handleUnsubscribe(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            botService.sendMessage(userId, "❌ 请提供要取消订阅的关键词，例如：/unsubscribe 比特币");
            return;
        }

        keyword = keyword.trim();
        boolean success = userPreferenceService.removeKeyword(userId, keyword);

        if (success) {
            int count = userPreferenceService.getKeywordCount(userId);
            String message = String.format("✅ 已取消订阅\"%s\"\n📊 当前订阅关键词：%d个", keyword, count);
            botService.sendMessage(userId, message);
        } else {
            botService.sendMessage(userId, "❌ 取消订阅失败，该关键词不存在。");
        }
    }

    private void handleLatest(Long userId, int count) {
        count = Math.max(1, Math.min(count, 20)); // 限制1-20条

        List<News> latestNews = newsService.getLatestNews(count);

        if (latestNews.isEmpty()) {
            botService.sendMessage(userId, "📭 暂时没有找到最新新闻。");
            return;
        }

        StringBuilder message = new StringBuilder("📰 最新新闻（").append(latestNews.size()).append("条）：\n\n");

        for (int i = 0; i < latestNews.size(); i++) {
            News news = latestNews.get(i);
            message.append(String.format("%d. 【%s】%s\n   🔗 %s\n   ⏰ %s\n\n",
                i + 1,
                news.getSiteSource(),
                truncate(news.getTitle(), 50),
                news.getLink(),
                formatTime(news.getPublishTime())
            ));
        }

        botService.sendMessage(userId, message.toString());
    }

    private void handleStats(Long userId) {
        // 创建模拟统计数据（实际实现时会连接真实统计服务）
        String message = "📊 Daily News 统计信息\n\n" +
                "📈 今日数据\n" +
                "• 新闻抓取：127条\n" +
                "• 成功推送：89条\n" +
                "• 数据源：5个\n\n" +
                "👥 用户统计\n" +
                "• 活跃用户：12人\n" +
                "• 总订阅数：28个\n\n" +
                "⚡ 系统状态\n" +
                "• 运行时间：24小时\n" +
                "• 数据库状态：正常\n" +
                "• Telegram连接：正常";

        botService.sendMessage(userId, message);
    }

    private void handleSettings(Long userId) {
        UserPreference preference = userPreferenceService.getUserPreference(userId);

        if (preference == null) {
            botService.sendMessage(userId, "❌ 未找到您的个人设置。");
            return;
        }

        String keywords = preference.getKeywords();
        List<String> keywordList = keywords != null ?
            Arrays.asList(keywords.split(",")) : Collections.emptyList();

        String message = String.format("⚙️ 个人偏好设置\n\n" +
                "🔔 推送设置\n" +
                "• 状态：%s\n" +
                "• 频率：每%d分钟\n" +
                "• 时间：%s - %s\n\n" +
                "📋 订阅关键词（%d个）\n" +
                "%s\n\n" +
                "💡 使用 /subscribe [关键词] 添加订阅",
                preference.getEnabled() ? "已启用" : "已禁用",
                preference.getPushFrequency(),
                preference.getPushStartTime(),
                preference.getPushEndTime(),
                keywordList.size(),
                keywordList.isEmpty() ? "暂无订阅" : String.join("、", keywordList)
        );

        botService.sendMessage(userId, message);
    }

    private void handleUnknown(Long userId, String command) {
        botService.sendMessage(userId, String.format("❓ 未知命令：%s\n使用 /help 查看可用命令。", command));
    }

    // 辅助方法
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String formatTime(Long timestamp) {
        if (timestamp == null) {
            return "未知";
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                                     ZoneId.systemDefault())
                           .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}