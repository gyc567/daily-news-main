package com.ll.news.site;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.LoadingCache;
import com.ll.news.common.CacheUtils;
import com.ll.news.common.ContentHandler;
import com.ll.news.common.EventPublish;
import com.ll.news.common.NewsConst;
import com.ll.news.model.News;
import com.ll.news.service.INewsService;
import com.ll.news.site.base.BaseSource;
import com.okx.connector.client.SpotClient;
import com.okx.connector.client.impl.SpotClientImpl;
import com.ytby.teams.TeamsMsgUtil;
import com.ytby.teams.model.AcAction;
import com.ytby.teams.template.CommonAcTemplate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
@Slf4j
public class OKXDelistingSource extends BaseSource {
    private static String baseUrl = "https://www.okx.com/";

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
        this.cache = CacheUtils.cache(newsService, NewsConst.Source.okx_delisting);
    }

    @Override
    public void refresh() throws Exception {
        SpotClient spotClient = new SpotClientImpl(new OkHttpClient(), "f90005ec-8551-4292-93ec-affa51262469", "9952F62B79D38AADE9964BE5EC590F87", "YuntouTest@1", false);

        Map<String, Object> params = new HashMap<>();
        params.put("annType", "announcements-delistings");
        params.put("page", "1");
        String res = spotClient.createAnnouncement().announcements(params);
        JSONObject jsonObject = JSON.parseObject(res);
        JSONArray data = jsonObject.getJSONArray("data");
        JSONArray announcements = ((JSONObject)(data.get(0))).getJSONArray("details");
        for (Object announcement : announcements) {
            JSONObject jsonObj = (JSONObject) announcement;
            String title = jsonObj.getString("title");
            String url = jsonObj.getString("url");
            Long pTime = jsonObj.getLong("pTime");
            grabArticle(title, url, pTime);
            System.out.println(url);
        }
    }

    private void grabArticle(String title, String href, Long pTime) {
        try {
            News news = new News();
            news.setSiteSource("okx_delisting");
            news.setPublishTime(System.currentTimeMillis());
            news.setStatus(NewsConst.Status.NEW);
            news.setTitle(title);
            news.setLink(href);
            news.setTags("[]");
            news.setContent(title);
            try {
                cache.get(href);
                log.info("skip link {}", href);
            } catch (Exception e) {
                log.info("{} not find, will insert", href);
                sendTeamsMsg(href, title, pTime);
                newsService.insertNews(news);
                eventPublish.publishNewsEvent(news);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendTeamsMsg(String url, String content, Long pTime) {
        List<AcAction> actions = new ArrayList<>();
        AcAction action = new AcAction("Action.OpenUrl", "查看公告详情", url);
        actions.add(action);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Timestamp(pTime));
        String msg = CommonAcTemplate.builder()
                .text("【Warning】币种下架预警", "Bolder", "Large", "Center", "Warning")
                .text(dateStr)
                .text(content)
                .actions(actions)
                .build();
        teamsMsgUtil.sendCommonTeamsMsg(msg);
    }
}
