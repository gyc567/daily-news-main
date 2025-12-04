package com.ll.news.bot.command;

import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /subscribe 命令 - 订阅关键词
 * 简化逻辑，消除复杂条件分支
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements Command {

    private final TelegramBot telegramBot;
    private final UserPreferenceService userPreferenceService;

    @Override
    public String getName() {
        return "/subscribe";
    }

    @Override
    public String getDescription() {
        return "订阅新闻关键词";
    }

    @Override
    public boolean requiresParameter() {
        return true;
    }

    @Override
    public void execute(Message message) {
        Long userId = message.from().id().longValue();
        String text = message.text();
        log.info("执行 /subscribe 命令，用户: {}", userId);

        // 提取关键词 - 消除边界情况
        String keyword = extractKeyword(text);
        if (keyword.isEmpty()) {
            sendErrorMessage(userId, "请提供要订阅的关键词，例如：/subscribe 比特币");
            return;
        }

        // 确保用户存在 - 无特殊情况
        userPreferenceService.initializeUser(userId);

        // 添加关键词 - 单一职责
        boolean success = userPreferenceService.addKeyword(userId, keyword);

        // 简洁的响应 - 消除复杂格式化
        if (success) {
            int count = userPreferenceService.getKeywordCount(userId);
            String response = String.format("✅ 成功订阅\"%s\"\n📊 当前订阅关键词：%d个\n🔔 将为您推送相关新闻",
                    keyword, count);
            telegramBot.execute(new SendMessage(userId, response));
        } else {
            sendErrorMessage(userId, "订阅失败，该关键词可能已存在或达到订阅上限（10个）。");
        }
    }

    /**
     * 提取关键词 - 消除复杂逻辑
     */
    private String extractKeyword(String text) {
        if (text == null) return "";

        String[] parts = text.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    /**
     * 发送错误消息 - 统一错误处理
     */
    private void sendErrorMessage(Long userId, String message) {
        telegramBot.execute(new SendMessage(userId, "❌ " + message));
    }
}