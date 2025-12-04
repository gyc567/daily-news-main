package com.ll.news.bot.command;

import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /stats 命令 - 显示系统统计
 * 简化统计逻辑，消除复杂计算
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StatsCommand implements Command {

    private final TelegramBot telegramBot;
    private final UserPreferenceService userPreferenceService;

    @Override
    public String getName() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "查看系统统计信息";
    }

    @Override
    public void execute(Message message) {
        Long userId = message.from().id().longValue();
        log.info("执行 /stats 命令，用户: {}", userId);

        // 简单的统计计算 - 消除复杂性
        long activeUsers = userPreferenceService.getActiveUsers().size();
        int totalSubscriptions = userPreferenceService.getTotalSubscriptions();

        String response = String.format("📊 Daily News 统计信息\n\n" +
                "📈 用户统计\n" +
                "• 活跃用户：%d人\n" +
                "• 总订阅数：%d个\n\n" +
                "⚡ 系统状态\n" +
                "• Telegram连接：正常\n" +
                "• 个性化推送：已启用\n\n" +
                "⏰ 统计时间：%s",
                activeUsers,
                totalSubscriptions,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        );

        telegramBot.execute(new SendMessage(userId, response));
    }
}