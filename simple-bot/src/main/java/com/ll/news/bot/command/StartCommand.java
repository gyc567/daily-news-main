package com.ll.news.bot.command;

import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /start 命令 - 用户欢迎和初始化
 * 简洁实现，消除边界情况
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final TelegramBot telegramBot;
    private final UserPreferenceService userPreferenceService;

    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "开始使用机器人";
    }

    @Override
    public void execute(Message message) {
        Long userId = message.from().id().longValue();
        log.info("执行 /start 命令，用户: {}", userId);

        // 简洁的欢迎消息 - 消除复杂格式
        String welcomeMessage = "👋 欢迎使用 Daily News 智能助手！\n\n" +
                "🤖 我可以为您提供：\n" +
                "• 📰 个性化新闻推送\n" +
                "• 🔍 智能关键词订阅\n" +
                "• ⚙️ 个人偏好设置\n\n" +
                "💡 使用 /help 查看所有可用命令\n" +
                "🎯 使用 /subscribe 开始个性化订阅";

        // 发送欢迎消息
        telegramBot.execute(new SendMessage(userId, welcomeMessage));

        // 初始化用户 - 无特殊情况
        userPreferenceService.initializeUser(userId);
    }
}