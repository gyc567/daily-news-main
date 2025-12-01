package com.ll.news.site;

import com.google.common.cache.LoadingCache;
import com.ll.drissonPage.element.SessionElement;
import com.ll.drissonPage.page.SessionPage;
import com.ll.news.common.CacheUtils;
import com.ll.news.common.ContentHandler;
import com.ll.news.common.EventPublish;
import com.ll.news.common.NewsConst;
import com.ll.news.model.News;
import com.ll.news.service.INewsService;
import com.ll.news.site.base.BaseSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BinanceNewsSource extends BaseSource {

    @Autowired
    ContentHandler filter;

    @Autowired
    INewsService newsService;

    @Autowired
    EventPublish eventPublish;

    private LoadingCache<String, News> cache;

    @PostConstruct
    public void init() {
        this.cache = CacheUtils.cache(newsService, NewsConst.Source.binance_new);
    }


    @Override
    public void refresh() throws InterruptedException {

        // link, content

        SessionPage page = new SessionPage();

        String url = NewsConst.Source.binance_new.link();
        String source = NewsConst.Source.binance_new.source();
        page.get(url);  // 访问网站

        SessionElement ele1 = page.ele(".:FeedList");

        List<SessionElement> eles = ele1.eles(".feed-buzz-card-base-view");
        for (SessionElement ele : eles) {

            SessionElement time = ele.ele(".create-time");
            String minutesStr = time.rawText().strip();
            String s = minutesStr.split(" ")[0];
            int duration = 0;
            if (!"--".equals(s)) {
                duration = Integer.parseInt(s);
            }
            long publishTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(duration);
            SessionElement titleEle = ele.ele(".:card__title");
            String title = titleEle.rawText().strip();
            SessionElement contentEle = ele.ele(".:card__description");
            String content = contentEle.rawText().strip();

            SessionElement element = ele.ele(".:feed-content-text");

            String href = element.ele("tag:a").attr("href");

            News news = new News();
            news.setSiteSource(source);
            news.setPublishTime(publishTime);
            news.setStatus(NewsConst.Status.NEW);
            news.setTitle(title);
            news.setLink(href);
            news.setTags("[]");
            news.setContent(content);

            try {
                cache.get(href);
                log.info("skip link {}", href);
            } catch (Exception e) {
                log.info("{} not find, will insert", href);
                newsService.insertNews(news);
                eventPublish.publishNewsEvent(news);
            }

        }

        page.close();

    }

}
