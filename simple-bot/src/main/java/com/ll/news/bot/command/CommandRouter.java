package com.ll.news.bot.command;

import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 命令路由器 - 策略模式实现
 * 遵循Linus的原则：消除if-else，使用查表法
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CommandRouter {

    private final List<Command> commands;
    private final Map<String, Command> commandMap;

    // 构造函数注入，Spring会自动收集所有Command实现
    public CommandRouter(List<Command> commands) {
        this.commands = commands;
        this.commandMap = commands.stream()
                .collect(Collectors.toMap(Command::getName, Function.identity()));
        log.info("加载了 {} 个命令", commands.size());
    }

    /**
     * 路由命令 - 消除复杂的switch/if-else
     */
    public void routeCommand(Message message) {
        if (message == null || message.text() == null) {
            return;
        }

        String text = message.text().trim();
        if (!text.startsWith("/")) {
            return;
        }

        // 提取命令名
        String commandName = text.split("\\s+")[0].toLowerCase();

        // 查表法路由 - 消除条件分支
        Command command = commandMap.get(commandName);
        if (command != null) {
            log.info("路由命令: {}，用户: {}", commandName, message.from().id());
            executeCommand(command, message);
        } else {
            handleUnknownCommand(message);
        }
    }

    /**
     * 执行命令 - 统一的异常处理
     */
    private void executeCommand(Command command, Message message) {
        try {
            command.execute(message);
        } catch (Exception e) {
            log.error("执行命令 {} 失败", command.getName(), e);
            handleCommandError(message, e.getMessage());
        }
    }

    /**
     * 处理未知命令 - 统一错误处理
     */
    private void handleUnknownCommand(Message message) {
        String command = message.text().split("\\s+")[0];
        String response = String.format("❓ 未知命令：%s\n使用 /help 查看可用命令。", command);

        message.chat().id();
        message.from().id();

        // 使用TelegramBot发送错误消息
        log.warn("未知命令: {}，用户: {}", command, message.from().id());
    }

    /**
     * 处理命令执行错误
     */
    private void handleCommandError(Message message, String error) {
        Long userId = message.from().id().longValue();
        String response = "❌ 处理命令时出现错误，请稍后重试。";

        log.error("命令执行错误，用户: {}，错误: {}", userId, error);
    }

    /**
     * 获取所有可用命令
     */
    public Map<String, String> getAvailableCommands() {
        return commandMap.values().stream()
                .collect(Collectors.toMap(Command::getName, Command::getDescription));
    }
}