package com.ll.news.bot;

import com.ll.news.bot.command.CommandRouter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 简化的Telegram更新处理器 - 使用策略模式
 * 遵循Linus的原则：消除复杂性，保持简单
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SimpleTelegramUpdateHandler implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final CommandRouter commandRouter;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        telegramBot.setUpdatesListener(this);
        log.info("🚀 Telegram更新监听器已启动 - 使用策略模式");
    }

    @Override
    public int process(java.util.List<Update> updates) {
        for (Update update : updates) {
            try {
                processUpdate(update);
            } catch (Exception e) {
                log.error("处理更新失败: {}", update, e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        // 只处理消息 - 消除复杂性
        if (update.message() != null) {
            processMessage(update.message());
        }

        // 忽略其他类型的更新 - 保持简单
    }

    private void processMessage(Message message) {
        // 只处理文本消息 - 单一职责
        if (message.text() == null) {
            return;
        }

        String text = message.text().trim();
        Long userId = message.from().id().longValue();

        // 处理命令 - 使用策略模式路由
        if (text.startsWith("/")) {
            log.info("收到命令: {}，用户: {}", text, userId);
            commandRouter.routeCommand(message);
        } else {
            // 处理普通消息 - 简单自动回复
            handleNormalMessage(message);
        }
    }

    /**
     * 处理普通消息 - 简化自动回复
     */
    private void handleNormalMessage(Message message) {
        String text = message.text().toLowerCase();
        Long userId = message.from().id().longValue();

        // 简单的关键词回复 - 消除复杂性
        String response = null;

        if (text.contains("你好") || text.contains("hi")) {
            response = "👋 你好！我是Daily News助手，使用 /help 查看可用命令。";
        } else if (text.contains("谢谢")) {
            response = "😊 不客气！随时为您服务。";
        } else if (text.contains("帮助")) {
            response = "💡 使用 /help 命令查看所有可用功能。";
        }

        // 只在有匹配时回复 - 避免噪音
        if (response != null) {
            telegramBot.execute(new com.pengrad.telegrambot.request.SendMessage(userId, response));
        }
    }
}