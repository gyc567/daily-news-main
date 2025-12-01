package com.ll.news.site;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.ytby.teams.TeamsMsgUtil;
import com.ytby.teams.model.AcAction;
import com.ytby.teams.template.CommonAcTemplate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@Component
@Slf4j
public class BinanceDelistingSource extends BaseSource {
    private static String baseUrl = "https://www.binance.com/zh-CN/support/announcement/";

    @Autowired
    ContentHandler filter;

    @Autowired
    INewsService newsService;

    @Autowired
    EventPublish eventPublish;

    @Autowired
    TeamsMsgUtil teamsMsgUtil;

    private LoadingCache<String, News> cache;
    @PostConstruct
    public void init() {
        this.cache = CacheUtils.cache(newsService, NewsConst.Source.binance_delisting);
    }


    @Override
    public void refresh() throws Exception {
        SessionPage page = new SessionPage();
        String url = NewsConst.Source.binance_delisting.link();
        page.get(url);
        SessionElement element = page.ele("#__APP_DATA");
        String val = element.innerEle().childNode(0).toString();
        JSONObject obj = JSONObject.parseObject(val);
        JSONObject appState = obj.getJSONObject("appState");
        JSONObject loader = appState.getJSONObject("loader");
        JSONObject dataByRouteId = loader.getJSONObject("dataByRouteId");
        JSONObject d9b2 = dataByRouteId.getJSONObject("d9b2");
        JSONArray catalogs = d9b2.getJSONArray("catalogs");
        for (Object catalog : catalogs) {
            JSONObject object = (JSONObject) catalog;
            if (object.getString("catalogName").equals("下架讯息")) {
                JSONArray articles = object.getJSONArray("articles");
                grabArticle(articles);
            } else {
                continue;
            }
        }
        page.close();
    }

    private void grabArticle(JSONArray articles) {
        for (Object article : articles) {
            SessionPage page = new SessionPage();
            JSONObject obj = (JSONObject) article;
            String code = obj.getString("code");
            String title = obj.getString("title");
            String url = baseUrl + title + "-" + code;
            url = url.replaceAll(" ", "");
            page.get(url);
            List<SessionElement> eles = page.eles("tag:head");
            if (eles.isEmpty()) {
             continue;
            }
            SessionElement head = eles.get(0);
            SessionElement description = head.ele("@name=description");
            String content = description.innerEle().attr("content");
            News news = new News();
            news.setSiteSource("binance_delisting");
            news.setPublishTime(System.currentTimeMillis());
            news.setStatus(NewsConst.Status.NEW);
            news.setTitle(title);
            news.setLink(url);
            news.setTags("[]");
            news.setContent(content);
            try {
                cache.get(url);
                log.info("skip link {}", url);
            } catch (Exception e) {
                log.info("{} not find, will insert", url);
                sendTeamsMsg(url, content);
                newsService.insertNews(news);
                eventPublish.publishNewsEvent(news);
            }
            page.close();
        }
    }

    private void sendTeamsMsg(String url, String content) {
        List<AcAction> actions = new ArrayList<>();
        AcAction action = new AcAction("Action.OpenUrl", "查看公告详情", url);
        actions.add(action);
        String msg = CommonAcTemplate.builder()
                .text("【Warning】币种下架预警", "Bolder", "Large", "Center", "Warning")
                .text(new Date().toString())
                .text(content)
                .actions(actions)
                .build();
        teamsMsgUtil.sendCommonTeamsMsg(msg);
    }
}
