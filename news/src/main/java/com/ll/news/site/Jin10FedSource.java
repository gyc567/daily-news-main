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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
@Slf4j
public class Jin10FedSource extends BaseSource {

    @Autowired
    ContentHandler filter;

    private static final int KEEPDAYS = 7;

    @Autowired
    INewsService newsService;

    @Autowired
    EventPublish eventPublish;

    private LoadingCache<String, News> jin10_fed_cache;

    @PostConstruct
    public void init() {
        this.jin10_fed_cache = CacheUtils.cache(newsService, NewsConst.Source.jin10_fed);
    }

    @Override
    public void refresh() {
        // link, content

        SessionPage page = new SessionPage();

        String url = NewsConst.Source.jin10_fed.link();
        String source = NewsConst.Source.jin10_fed.source();
        page.get(url);  // 访问网站

        SessionElement ele1 = page.ele("@class=jin10-news-list");
        List<SessionElement> eles = ele1.eles("@class=jin10-news-list-item-info");


        List<String> list = new ArrayList<>();

        for (SessionElement ele : eles) {
            SessionElement eled = ele.ele("tag:a");
            String link = eled.link();
            if (link.contains("/flash/")) {
                continue;
            }
            if (StringUtils.isNoneEmpty(link)) {
                if (jin10_fed_cache.getIfPresent(link) != null) {
                    log.info("skip link {}", link);
                    continue;
                }
                list.add(link);
            }

        }

        for (String s : list) {
            try {
                page.get(s, 5.0);
                SessionElement sessionElement = page.ele("@class=jin10-news-cdetails");
                SessionElement content = sessionElement.ele("@class=jin10-news-cdetails-content");
                SessionElement element = sessionElement.child("tag:div");
                SessionElement title = sessionElement.ele("@class=news-app_title");

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(8)));

                Date date = new Date();
//                try {
//                    date = simpleDateFormat.parse(time.rawText().strip());
//                } catch (ParseException e) {
//                    log.warn(e.getMessage());
//                }

                long time1 = date.getTime();

                String rawText = content.rawText();
                List<String> stringList = rawText.lines().map(String::strip).filter(t -> {
                    return StringUtils.isNoneBlank(t) && !t.contains("下载mp3") && !t.startsWith("联系商务合作") && !t.startsWith("风险提示及免责条款");
                }).toList();
                String join = String.join("\n", stringList);
                News news = new News();
                news.setPublishTime(time1);
                String title1 = title.rawText();
                news.setTitle(title1);
                news.setStatus(NewsConst.Status.NEW);
                news.setLink(s);
                news.setSiteSource(source);
                news.setContent(join);
                news.setTags("[]");

                try {
                    jin10_fed_cache.get(s);
                    log.info("skip link {}", s);
                } catch (Exception e) {
                    log.info("{} not find, will insert", s);
                    newsService.insertNews(news);
                    eventPublish.publishNewsEvent(news);
                }

            } catch (Exception e) {
                log.warn("link {} error", s, e);
            }
        }

        page.close();

    }

}
