package com.ll.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ll.news.mapper.NewsMapper;
import com.ll.news.model.News;
import com.ll.news.service.INewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class NewsService implements INewsService {

    @Autowired
    NewsMapper newsMapper;

    @Override
    public boolean existNewsBySourceAndLink(String source, String link) {
        LambdaQueryWrapper<News> query = Wrappers.lambdaQuery(News.class);
        query.eq(News::getSiteSource, source);
        query.eq(News::getLink, link);
        return newsMapper.exists(query);
    }

    @Override
    public News selectBySourceAndLink(String source, String link) {
        LambdaQueryWrapper<News> query = Wrappers.lambdaQuery(News.class);
        query.eq(News::getSiteSource, source);
        query.eq(News::getLink, link);
        return newsMapper.selectOne(query);
    }

    @Override
    public int insertNews(News news) {
        return newsMapper.insert(news);
    }

    @Override
    public int updateById(News news) {
        return newsMapper.updateById(news);
    }

    @Override
    public int deleteNewsById(Long id) {
        return newsMapper.deleteById(id);
    }

    @Override
    public int deleteNewsByIds(List<Long> ids) {
        return newsMapper.deleteByIds(ids);
    }

    @Override
    public List<News> expireIds(String source, String link, long delayMills) {
        LambdaQueryWrapper<News> query = Wrappers.lambdaQuery(News.class);
        query.select(News::getId);
        query.eq(News::getSiteSource, source);
        query.eq(News::getLink, link);
        query.lt(News::getPublishTime, System.currentTimeMillis() - delayMills);
        return newsMapper.selectList(query);
    }




}
