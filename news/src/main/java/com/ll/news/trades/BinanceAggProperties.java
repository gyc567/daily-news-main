package com.ll.news.trades;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(value = "binance.agg-trades")
@Component
@Data
public class BinanceAggProperties {

    //     tg:
    //      token: 7800570468:AAFunJqTi_qmWH3zAy7u31TGo2wK7w0hrfc
    //      chatId: -1002555659999
    //    seconds: 300
    //    amt:
    //      ETH: 10000000
    //      BTC: 10000000
    private Map<String, String> tg;

    private Long seconds;

    private Double limit;

    private Integer count;

    private Map<String, Long> amt;


}
