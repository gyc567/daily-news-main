package com.ll.news.config;

import com.ll.news.model.News;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Locked;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSet {

    private static final int KEEP_DAYS = 7;

    /**
     * 来源
     */
    private String source;

    /**
     * 消息列表
     */
    private List<News> newsList = new ArrayList<>();

    public void addNews(News news) {
        newsList.add(news);
    }

    @Locked
    public void clean() {
        newsList.removeIf(news -> System.currentTimeMillis() - news.getPublishTime() > Duration.ofDays(KEEP_DAYS).toMillis());
    }

}
