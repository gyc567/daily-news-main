package com.ll.news.common;

import com.ll.news.model.News;
import com.ll.news.service.INewsService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class NewsEventListener {

    @Value("${app.telegram.chatId}")
    private String chatId;

    @Autowired
    TelegramBot bot;

    @Autowired
    INewsService newsService;

    @EventListener(classes = NewsEvent.class)
    @Async("msgExecutor")
    public void newsEventListener(NewsEvent newsEvent) {
        News news = newsEvent.getNews();
        String siteSource = news.getSiteSource();
        NewsConst.Source bySource = NewsConst.Source.getBySource(siteSource);
        String title = "";
        if (Objects.nonNull(bySource)) {
            title = bySource.desc() + ": ";
        }


//        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
//                new InlineKeyboardButton[]{
//                        new InlineKeyboardButton("AI总结").callbackData("ai_sum"),
//                });

        SendMessage request = new SendMessage(chatId, "[" + title + news.getTitle() + "](" +news.getLink() + ")\n\n" + news.getContent())
                .parseMode(ParseMode.Markdown)
                .disableNotification(true)
                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true))
//                .replyMarkup(inlineKeyboard)
                ;

        SendResponse execute = bot.execute(request);

        if (execute.isOk()) {
            news.setStatus(NewsConst.Status.PUBLISHED);
            newsService.updateById(news);
        }

    }


}
