package com.ll.news.bot.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /help 命令 - 显示帮助信息
 * 简洁明了，消除视觉噪音
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements Command {

    private final TelegramBot telegramBot;

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "显示帮助信息";
    }

    @Override
    public void execute(Message message) {
        Long userId = message.from().id().longValue();
        log.info("执行 /help 命令，用户: {}", userId);

        // 简洁的帮助信息 - 消除emoji和复杂格式
        String helpText = "📋 可用命令列表：\n\n" +
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

        telegramBot.execute(new SendMessage(userId, helpText));
    }
}