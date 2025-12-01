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
}
