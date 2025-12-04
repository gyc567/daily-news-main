package com.ll.news.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Telegram机器人服务类
 * 封装Telegram Bot的常用操作
 */
@Service
@Slf4j
public class TelegramBotService {

    @Autowired
    private TelegramBot bot;

    /**
     * 发送文本消息给用户
     */
    public boolean sendMessage(Long userId, String text) {
        return sendMessage(userId.toString(), text);
    }

    /**
     * 发送文本消息给聊天
     */
    public boolean sendMessage(String chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text);
            // 如果消息太长，进行截断
            if (text.length() > 4096) {
                text = text.substring(0, 4093) + "...";
                request = new SendMessage(chatId, text);
            }

            SendResponse response = bot.execute(request);

            if (response.isOk()) {
                log.debug("消息发送成功给聊天{}：{}", chatId, truncateText(text, 50));
                return true;
            } else {
                log.error("消息发送失败给聊天{}：{} - {}",
                         chatId, response.errorCode(), response.description());
                return false;
            }
        } catch (Exception e) {
            log.error("发送消息给聊天{}时发生异常：{}", chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送Markdown格式的消息
     */
    public boolean sendMarkdownMessage(String chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.Markdown);

            SendResponse response = bot.execute(request);

            if (response.isOk()) {
                log.debug("Markdown消息发送成功给聊天{}：{}", chatId, truncateText(text, 50));
                return true;
            } else {
                log.error("Markdown消息发送失败给聊天{}：{} - {}",
                         chatId, response.errorCode(), response.description());
                return false;
            }
        } catch (Exception e) {
            log.error("发送Markdown消息给聊天{}时发生异常：{}", chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送HTML格式的消息
     */
    public boolean sendHtmlMessage(String chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.HTML);

            SendResponse response = bot.execute(request);

            if (response.isOk()) {
                log.debug("HTML消息发送成功给聊天{}：{}", chatId, truncateText(text, 50));
                return true;
            } else {
                log.error("HTML消息发送失败给聊天{}：{} - {}",
                         chatId, response.errorCode(), response.description());
                return false;
            }
        } catch (Exception e) {
            log.error("发送HTML消息给聊天{}时发生异常：{}", chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 截断文本用于日志记录
     */
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}