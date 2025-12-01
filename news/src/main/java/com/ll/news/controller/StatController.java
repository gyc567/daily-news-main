package com.ll.news.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class StatController {

//    @Autowired
//    private DailyCrawler dailyCrawler;
//
//    @PostConstruct
//    public void bitcoinEntitiesCrawl() {
//        dailyCrawler.bitcoinEntitiesCrawl();
//    }

    @RequestMapping
    public String status() {
        return "ok";
    }

}
