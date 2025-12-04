package com.ll.news.bot.command;

import com.pengrad.telegrambot.model.Message;

/**
 * 命令接口 - 策略模式
 * 遵循Linus的原则：每个类只做一件事，做好一件事
 */
public interface Command {

    /**
     * 获取命令名称
     */
    String getName();

    /**
     * 获取命令描述
     */
    String getDescription();

    /**
     * 执行命令
     */
    void execute(Message message);

    /**
     * 是否需要参数
     */
    default boolean requiresParameter() {
        return false;
    }
}