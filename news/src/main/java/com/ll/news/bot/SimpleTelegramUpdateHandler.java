package com.ll.news.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 简化的Telegram更新处理器
 * 专注于核心机器人功能
 */
@Component
@Slf4j
public class SimpleTelegramUpdateHandler implements UpdatesListener {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private SimpleTelegramCommandHandler commandHandler;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        log.info("Telegram更新监听器已启动");
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
        // 处理消息
        if (update.message() != null) {
            processMessage(update.message());
        }

        // 处理回调查询（如果需要）
        if (update.callbackQuery() != null) {
            processCallbackQuery(update.callbackQuery());
        }
    }

    private void processMessage(Message message) {
        // 只处理文本消息
        if (message.text() == null) {
            return;
        }

        String text = message.text().trim();
        Long userId = message.from().id().longValue();

        // 处理命令
        if (text.startsWith("/")) {
            log.info("收到命令: {} 来自用户: {}", text, userId);
            commandHandler.handleCommand(message);
        } else {
            // 处理普通消息
            handleNormalMessage(message);
        }
    }

    private void handleNormalMessage(Message message) {
        String text = message.text();
        Long userId = message.from().id().longValue();

        // 简单的自动回复
        String response = generateAutoResponse(text);

        if (response != null) {
            telegramBot.execute(new com.pengrad.telegrambot.request.SendMessage(userId, response));
        }
    }

    private String generateAutoResponse(String text) {
        text = text.toLowerCase();

        if (text.contains("你好") || text.contains("hi") || text.contains("hello")) {
            return "👋 你好！我是Daily News助手，使用 /help 查看可用命令。";
        }

        if (text.contains("谢谢") || text.contains("thanks")) {
            return "😊 不客气！随时为您服务。";
        }

        if (text.contains("帮助") || text.contains("help")) {
            return "💡 使用 /help 命令查看所有可用功能。";
        }

        return null; // 不回复未知消息
    }

    private void processCallbackQuery(com.pengrad.telegrambot.model.CallbackQuery callbackQuery) {
        // 处理内联键盘回调（如果需要）
        log.info("收到回调查询: {} 来自用户: {}", callbackQuery.data(), callbackQuery.from().id());

        // 回复回调
        telegramBot.execute(new com.pengrad.telegrambot.request.AnswerCallbackQuery(callbackQuery.id()));
    }
}