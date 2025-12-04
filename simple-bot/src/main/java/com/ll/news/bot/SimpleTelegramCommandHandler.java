package com.ll.news.bot;

import com.ll.news.entity.SimpleUserPreference;
import com.ll.news.service.SimpleUserPreferenceService;
import com.pengrad.telegrambot.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 简化的Telegram命令处理器
 * 不依赖复杂的数据库和外部服务
 */
@Component
@Slf4j
public class SimpleTelegramCommandHandler {

    @Autowired
    private TelegramBotService botService;

    @Autowired
    private SimpleUserPreferenceService userPreferenceService;

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
                case "/settings":
                    handleSettings(userId);
                    break;
                case "/stats":
                    handleStats(userId);
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
                "• 🔍 智能关键词订阅\n" +
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
                "⚙️ 个人设置\n" +
                "/settings - 查看个人偏好设置\n\n" +
                "📊 数据统计\n" +
                "/stats - 查看系统统计信息\n\n" +
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
            botService.sendMessage(userId, "❌ 订阅失败，该关键词可能已存在或达到订阅上限（10个）。");
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

    private void handleSettings(Long userId) {
        SimpleUserPreference preference = userPreferenceService.getUserPreference(userId);

        if (preference == null) {
            botService.sendMessage(userId, "❌ 未找到您的个人设置。");
            return;
        }

        String keywords = preference.getKeywords();
        String keywordList = keywords != null && !keywords.isEmpty() ? keywords : "暂无订阅";

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
                formatTime(preference.getPushStartTime()),
                formatTime(preference.getPushEndTime()),
                preference.getKeywordList().size(),
                keywordList
        );

        botService.sendMessage(userId, message);
    }

    private void handleStats(Long userId) {
        // 模拟统计数据
        String message = "📊 Daily News 统计信息\n\n" +
                "📈 今日数据\n" +
                "• 活跃用户：" + userPreferenceService.getActiveUsers().size() + "人\n" +
                "• 总订阅数：" + getTotalSubscriptions() + "个\n\n" +
                "⚡ 系统状态\n" +
                "• Telegram连接：正常\n" +
                "• 个性化推送：已启用";

        botService.sendMessage(userId, message);
    }

    private void handleUnknown(Long userId, String command) {
        botService.sendMessage(userId, String.format("❓ 未知命令：%s\n使用 /help 查看可用命令。", command));
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private int getTotalSubscriptions() {
        int total = 0;
        for (SimpleUserPreference user : userPreferenceService.getActiveUsers()) {
            total += user.getKeywordList().size();
        }
        return total;
    }
}