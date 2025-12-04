package com.ll.news.common;

import com.ll.news.bot.TelegramCommandHandler;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class BotMsgHandler {

    @Value("${app.telegram.chatId}")
    private String chatId;

    @Autowired
    TelegramBot bot;

    @Autowired
    private TelegramCommandHandler commandHandler;

    @PostConstruct
    public void init() {

        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                for (Update update : updates) {
                    try {
                        // 优先处理消息命令
                        if (update.hasMessage() && update.getMessage().hasText()) {
                            Message message = update.getMessage();
                            String text = message.text();

                            // 处理以/开头的命令
                            if (text != null && text.startsWith("/")) {
                                log.info("收到命令：{}，来自用户：{}", text, message.from().id());
                                commandHandler.handleCommand(message);
                                continue;  // 命令处理完后继续处理下一个更新
                            }
                        }

                        // 处理回调查询（原有功能）
                        CallbackQuery callbackQuery = update.callbackQuery();
                        if (Objects.nonNull(callbackQuery)) {
                            String data = callbackQuery.data();
                            if (data.equals("ai_sum")) {
                                MaybeInaccessibleMessage maybeInaccessibleMessage = callbackQuery.maybeInaccessibleMessage();
                                Integer i = maybeInaccessibleMessage.messageId();
                                Chat chat = maybeInaccessibleMessage.chat();
                                Long id = chat.id();
                                if (!Objects.equals(chatId, id.toString())) {
                                    continue;
                                }
                                if (maybeInaccessibleMessage instanceof Message) {
                                    Message msg = (Message) maybeInaccessibleMessage;

                                    EditMessageText editedit = new EditMessageText(id, i, msg.text() + "\n\n" + "~等待接入AI~");
                                    editedit.parseMode(ParseMode.Markdown);
                                    BaseResponse execute = bot.execute(editedit);
                                    String description = execute.description();
                                    System.out.println(description);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理更新时异常：{}", e.getMessage(), e);
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        }, new ExceptionHandler() {
            public void onException(TelegramException e) {
                if (e.response() != null) {
                    // got bad response from telegram
                    e.response().errorCode();
                    e.response().description();
                } else {
                    // probably network error
                    log.error(e.getMessage());
                }
            }
        });

    }


}
