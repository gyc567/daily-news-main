package com.ll.news.site;

import com.ll.news.common.NewsConst;
import com.ll.news.model.News;
import com.ll.news.service.INewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CleanService {

    @Autowired
    INewsService newsService;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpireNews() {
        for (NewsConst.Source source : NewsConst.Source.values()) {
            String source1 = source.source();
            String link = source.link();

            List<News> newsList = newsService.expireIds(source1, link, TimeUnit.DAYS.toMillis(30));

            if (newsList.isEmpty()) {
                return;
            }
            List<Long> list = newsList.stream().map(News::getId).toList();
            newsService.deleteNewsByIds(list);
        }
    }


}
