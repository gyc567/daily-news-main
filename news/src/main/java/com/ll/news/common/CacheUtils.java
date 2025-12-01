package com.ll.news.common;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ll.news.model.News;
import com.ll.news.service.INewsService;

import java.util.Objects;

public class CacheUtils {

    public static LoadingCache<String, News> cache(INewsService newsService, NewsConst.Source source) {
        return com.google.common.cache.CacheBuilder.
                newBuilder().
                maximumSize(100)
                .build(new CacheLoader<String, News>() {
                    @Override
                    public News load(String key) throws Exception {  //动态加载缓存
                        News news = newsService.selectBySourceAndLink(source.source(), key);
                        if (Objects.isNull(news)) {
                            throw new RuntimeException("not find");
                        }
                        return news;
                    }
                });
    }


}
