package com.ll.news.trades;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ll.news.common.wss.WssConnectOption;
import com.ll.news.common.wss.WssConnector;
import com.ll.news.common.wss.WssContext;
import com.ll.news.common.wss.quote.KeepAliveBaseWssHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.val;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class BinanceAggTradesCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BinanceAggTradesCheck.class);

    @Autowired
    TaskExecutor msgExecutor;

    @Autowired
    BinanceAggProperties aggProperties;

    @Autowired
    OkHttpClient okHttpClient;

    TelegramBot bot;

    @Autowired
    private WebSocketClient webSocketClient;
    @Autowired
    private Vertx vertx;

    ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        bot = new TelegramBot(aggProperties.getTg().get("token"));

    }

    @Data
    static class Stat {

        public Stat(String symbol, String currency, String type) {
            this.symbol = symbol;
            this.currency = currency;
            this.type = type;
        }

        private String symbol;
        private String currency;
        private String type;

        private Double amt = 0.0;
        private JSONObject last;
        private List<JSONObject> eventList = new ArrayList<>();

        public String getAmtStr() {
            double v = amt / 1_000_000;
            return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        }

        public String getVolStr() {
            Double qty = last.getDouble("q");
            return qty.toString();
        }

        public String getPriceStr() {
            Double price = last.getDouble("p");
            return price.toString();
        }

        public String getSide() {
            boolean sell = last.getBoolean("m");
            return sell ? "主动卖出" : "主动买入";
        }


        public Pair<String, String> durationCheck(Double limit, int count) {
            if (eventList.isEmpty()) {
                return null;
            }
            List<JSONObject> sellSideList = new ArrayList<>();
            List<JSONObject> buySideList = new ArrayList<>();
            for (JSONObject jsonObject : eventList) {
                Boolean m = jsonObject.getBoolean("m");
                if (m) {
                    sellSideList.add(jsonObject);
                } else {
                    buySideList.add(jsonObject);
                }
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(8)));
            String timeStr = format.format(new Date());

            // 2、监控1分钟内大于1M美金的吃单数量，单一方向大于5条的时候报警提示。
            //必要的告警参数：币种，一分钟内大于1M的单子数量，方向，总金额。

            String sellStr = getString(symbol, type, limit, count, sellSideList, timeStr, "主动卖出");
            String buyStr = getString(symbol, type, limit, count, buySideList, timeStr, "主动买入");

            return Pair.of(sellStr, buyStr);
        }

        private static @Nullable String getString(String symbol, String type, Double limit, int count, List<JSONObject> sellSideList, String timeStr, String sideStr) {
            double sellAmt = 0.0;
            double sellQty = 0.0;
            int sellCount = 0;
            for (JSONObject jsonObject : sellSideList) {
                Double price = jsonObject.getDouble("p");
                Double qty = jsonObject.getDouble("q");
                double sumAmt = price * qty;
                if (sumAmt > limit * 1_000_000) {
                    sellAmt += sumAmt;
                    sellCount++;
                    sellQty += qty;
                }
            }

            String sellStr = null;
            if (sellCount >= count) {
                sellStr = StrUtil.format(
                        "#累计1分钟触发通知 \n" +
                                "*交易所*: {} \n" +
                                "*类型*: {} \n" +
                                "*时间*: {} \n" +
                                "*币对*: {} \n" +
                                "*方向*: {} \n" +
                                "*次数*: {} \n" +
                                "*总成交量*: {} \n" +
                                "*总成交额*: {}M \n" +
                                "{}",
                        "Binance", type, timeStr, symbol, sideStr, sellCount, new BigDecimal(sellAmt).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),
                        new BigDecimal(sellQty).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(), "#累计 " + " #" + symbol + " #" + sideStr + " #" + type);
            }
            return sellStr;
        }


        public boolean update(JSONObject jsonObject, Long seconds, Long amtLimit) {
            Boolean m = jsonObject.getBoolean("m");
//            if (!m) {
//                return false;
//            }

            last = jsonObject;

            Double price = jsonObject.getDouble("p");
            Double qty = jsonObject.getDouble("q");
            double sumAmt = qty;

            long millis = System.currentTimeMillis();
            for (Iterator<JSONObject> iterator = eventList.iterator(); iterator.hasNext(); ) {
                JSONObject object = iterator.next();
                Long time = object.getLong("E");
                if (millis - time > seconds * 1000) {
                    iterator.remove();
                }
            }
            eventList.add(jsonObject);

            this.amt = qty * price;
            return qty >= amtLimit;
        }
    }

    // spot-symbol, amt
    private final Map<String, Stat> statMap = new ConcurrentHashMap<>();


    @Override
    public void run(ApplicationArguments args) throws Exception {

        listenSpot();

        listenUm();

        vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), t -> {
            for (Map.Entry<String, Stat> entry : statMap.entrySet()) {
                Stat stat = entry.getValue();
                Pair<String, String> durationCheck = stat.durationCheck(aggProperties.getLimit(), aggProperties.getCount());

                if (durationCheck != null) {
                    String left = durationCheck.getLeft();
                    String right = durationCheck.getRight();
                    msgExecutor.execute(() -> {
                        if (Objects.nonNull(left)) {
                            sendMsgAsync(left);
                        }
                        if (Objects.nonNull(right)) {
                            sendMsgAsync(right);
                        }
                    });
                }
            }
        });


    }

    private void listenSpot() {
        WssConnectOption wssConnectOption = new WssConnectOption();
        wssConnectOption.setPort(443);
        wssConnectOption.setHost("stream.binance.com");
        wssConnectOption.setUri("/stream?streams=btcusdt@aggTrade/ethusdt@aggTrade");
        wssConnectOption.setOrigUri("/stream?streams=btcusdt@aggTrade/ethusdt@aggTrade");
        wssConnectOption.setSsl(true);
        new WssConnector(webSocketClient, new WssContext(wssConnectOption, vertx), new KeepAliveBaseWssHandler() {
            @Override
            public void onTextMsg(WebSocket webSocket, String text) {
                JSONObject jsonObject = JSONObject.parseObject(text);
                JSONObject data = jsonObject.getJSONObject("data");
                if (Objects.nonNull(data)) {
                    String symbol = data.getString("s");
                    String currency = symbol.replace("USDT", "");
                    String type = "现货";
                    threadExecutor.execute(() -> handleMsg(symbol, currency, type, data));
                }
            }
        }).reconnect();
    }

    private void listenUm() {
        WssConnectOption wssConnectOption = new WssConnectOption();
        wssConnectOption.setPort(443);
        wssConnectOption.setHost("fstream.binance.com");
        wssConnectOption.setUri("/stream?streams=btcusdt@aggTrade/ethusdt@aggTrade");
        wssConnectOption.setOrigUri("/stream?streams=btcusdt@aggTrade/ethusdt@aggTrade");
        wssConnectOption.setSsl(true);
        new WssConnector(webSocketClient, new WssContext(wssConnectOption, vertx), new KeepAliveBaseWssHandler() {
            @Override
            public void onTextMsg(WebSocket webSocket, String text) {
                JSONObject jsonObject = JSONObject.parseObject(text);
                JSONObject data = jsonObject.getJSONObject("data");
                if (Objects.nonNull(data)) {
                    String symbol = data.getString("s");
                    String currency = symbol.replace("USDT", "");
                    String type = "U本位";
                    threadExecutor.execute(() -> handleMsg(symbol, currency, type, data));
                }
            }
        }).reconnect();
    }

    private void handleMsg(String symbol, String currency, String type, JSONObject data) {
        String key = type + "_" + symbol;
        Stat stat = statMap.computeIfAbsent(key, k -> new Stat(symbol, currency, type));
        Long amtLimit = aggProperties.getAmt().get(currency);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(8)));
        String timeStr = format.format(new Date());

        boolean needNotifySingle = stat.update(data, aggProperties.getSeconds(), amtLimit);

        if (needNotifySingle) {
            log.info("trade notify {}", data.toJSONString());

            String amt = stat.getAmtStr();
            String volStr = stat.getVolStr();
            String priceStr = stat.getPriceStr();
            String side = stat.getSide();

            String binance = StrUtil.format(
                    "#单笔触发通知 \n" +
                            "*交易所*: {} \n" +
                            "*类型*: {} \n" +
                            "*时间*: {} \n" +
                            "*币对*: {} \n" +
                            "*方向*: {} \n" +
                            "*成交量*: {} \n" +
                            "*成交价*: {} \n" +
                            "*成交额*: {}M \n" +
                            "{}",
                    "Binance", type, timeStr, symbol, side, volStr, priceStr, amt, "#单笔" + " #" + symbol + " #" + side + " #" + type);

            sendMsgAsync(binance);
        }
    }

    private void sendMsgAsync(String left) {
        SendMessage request = new SendMessage(aggProperties.getTg().get("chatId"), left)
                .parseMode(ParseMode.Markdown)
                .disableNotification(false)
                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true))
//                .replyMarkup(inlineKeyboard)
                ;
        SendResponse execute = bot.execute(request);
        if (!execute.isOk()) {
            log.warn("send tg fail {}", execute.message());
        }
    }

    public static void main(String[] args) {
        val telegramBot = new TelegramBot("7800570468:AAFunJqTi_qmWH3zAy7u31TGo2wK7w0hrfc");
        SendMessage request = new SendMessage("-1002555659999L",
                StrUtil.format("test msg {}", "#示例 #TelegramBot")
        )
                .parseMode(ParseMode.Markdown)
                .disableNotification(false)
                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
        val execute = telegramBot.execute(request);

        System.out.println(execute.message());


    }

}
