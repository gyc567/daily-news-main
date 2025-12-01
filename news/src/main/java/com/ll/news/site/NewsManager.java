package com.ll.news.site;


import com.ll.news.site.base.BaseSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NewsManager {

    @Autowired
    List<BaseSource> sources;

    @Scheduled(initialDelay = 5000, fixedDelay = 30_000)
    public void refresh() {
        for (BaseSource source : sources) {

            try {
                source.refresh();
            } catch (Exception e) {
                log.error("error {}", source.getClass().getSimpleName(), e);
            }

        }
    }


}
