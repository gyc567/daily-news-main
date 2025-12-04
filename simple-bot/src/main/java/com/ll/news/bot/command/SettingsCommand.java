package com.ll.news.bot.command;

import com.ll.news.entity.UserPreference;
import com.ll.news.service.UserPreferenceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * /settings 命令 - 显示个人偏好设置
 * 简化数据展示，消除复杂格式化
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SettingsCommand implements Command {

    private final TelegramBot telegramBot;
    private final UserPreferenceService userPreferenceService;

    @Override
    public String getName() {
        return "/settings";
    }

    @Override
    public String getDescription() {
        return "查看个人偏好设置";
    }

    @Override
    public void execute(Message message) {
        Long userId = message.from().id().longValue();
        log.info("执行 /settings 命令，用户: {}", userId);

        // 获取用户偏好 - 单一职责
        Optional<UserPreference> optional = userPreferenceService.getUserPreference(userId);

        if (optional.isEmpty()) {
            sendNotFoundMessage(userId);
            return;
        }

        UserPreference preference = optional.get();
        String response = buildSettingsResponse(preference);
        telegramBot.execute(new SendMessage(userId, response));
    }

    /**
     * 构建设置响应 - 消除复杂格式化
     */
    private String buildSettingsResponse(UserPreference preference) {
        String[] keywords = preference.getKeywordList();
        String keywordList = keywords.length > 0
                ? String.join(", ", keywords)
                : "暂无订阅";

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return String.format("⚙️ 个人偏好设置\n\n" +
                "🔔 推送设置\n" +
                "• 状态：%s\n" +
                "• 频率：每%d分钟\n" +
                "• 时间：%s - %s\n\n" +
                "📋 订阅关键词（%d个）\n" +
                "%s\n\n" +
                "💡 使用 /subscribe [关键词] 添加订阅",
                preference.getEnabled() ? "已启用" : "已禁用",
                preference.getPushFrequency(),
                preference.getPushStartTime().format(timeFormatter),
                preference.getPushEndTime().format(timeFormatter),
                keywords.length,
                keywordList
        );
    }

    /**
     * 发送未找到消息
     */
    private void sendNotFoundMessage(Long userId) {
        String response = "❌ 未找到您的个人设置。\n请先使用 /start 命令初始化。";
        telegramBot.execute(new SendMessage(userId, response));
    }
}