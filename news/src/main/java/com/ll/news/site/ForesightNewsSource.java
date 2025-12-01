package com.ll.news.site;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.LoadingCache;
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

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@Component
@Slf4j
public class ForesightNewsSource extends BaseSource {

    @Autowired
    ContentHandler filter;

    @Autowired
    INewsService newsService;

    @Autowired
    EventPublish eventPublish;

    private LoadingCache<String, News> cache;

    @PostConstruct
    public void init() {
        cache = CacheUtils.cache(newsService, NewsConst.Source.foresightNews_quick_news);
    }

    @Override
    public void refresh() {

        SessionPage page = new SessionPage();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(0)));
        String timeQuery = simpleDateFormat.format(new Date());

        String url = "https://api.foresightnews.pro/v1/dayNews?date=" + timeQuery;

        String detailUrl = "https://foresightnews.pro/news/detail/";

        String source = NewsConst.Source.foresightNews_quick_news.source();
        page.get(url);  // 访问网站

        JSONObject json = page.json();
        String data = json.getString("data");
        byte[] decode = Base64.decode(data);
        byte[] bytes = ZipUtil.unZlib(decode);
        String content = new String(bytes);

        JSONArray dataArray = (JSONArray) JSONArray.parse(content);

        for (Object o : dataArray) {
            JSONObject jsonObject = (JSONObject) o;
            JSONArray news = jsonObject.getJSONArray("news");
            for (Object object : news) {
                JSONArray tagArray = new JSONArray();
                JSONObject newsItem = (JSONObject) object;
                String id = newsItem.getString("id");
                String title = newsItem.getString("title");
                String contentStr = newsItem.getString("brief");
                Long published_at = newsItem.getLong("published_at") * 1000;
                String detailUrlItem = detailUrl + id;
                JSONArray tags = newsItem.getJSONArray("tags");
                if (Objects.isNull(tags)) {
                    continue;
                }
                for (Object tag : tags) {
                    JSONObject tagObj = (JSONObject) tag;
                    String tagName = tagObj.getString("name");
                    tagArray.add(tagName);
                }

                News newsInsert = News.builder()
                        .siteSource(source)
                        .link(detailUrlItem)
                        .publishTime(published_at)
                        .title(title)
                        .status(NewsConst.Status.NEW)
                        .content(contentStr)
                        .tags(tagArray.toString())
                        .build();

                try {
                    cache.get(detailUrlItem);
                    log.info("skip link {}", detailUrlItem);
                } catch (Exception e) {
                    log.info("{} not find, will insert", detailUrlItem);
                    newsService.insertNews(newsInsert);
                    eventPublish.publishNewsEvent(newsInsert);
                }
            }

        }

        page.close();

    }

}
