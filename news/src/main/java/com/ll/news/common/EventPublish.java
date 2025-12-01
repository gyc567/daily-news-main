package com.ll.news.common;

import com.ll.news.model.News;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublish {

    @Autowired
    ApplicationEventPublisher publisher;

    public void publishNewsEvent(News news) {
        NewsEvent event = new NewsEvent();
        event.setNews(news);
        publisher.publishEvent(event);
    }

}
