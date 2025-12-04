package com.ll.news.service;

import com.ll.news.model.News;

import java.util.List;

public interface INewsService {
    boolean existNewsBySourceAndLink(String source, String link);

    News selectBySourceAndLink(String source, String link);

    int insertNews(News news);

    int updateById(News news);

    int deleteNewsById(Long id);

    int deleteNewsByIds(List<Long> ids);

    List<News> expireIds(String source, String link, long delayMills);

    /**
     * 获取最新新闻
     * @param count 新闻数量
     * @return 最新新闻列表
     */
    List<News> getLatestNews(int count);
}
