package com.ll.news.bitcoin.crawler;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ll.news.bitcoin.EntityTypeEnum;
import com.ll.news.bitcoin.domain.BitcoinEntitiesDetail;
import com.ll.news.bitcoin.domain.BitcoinEntitiesSummary;
import com.ll.news.bitcoin.domain.BitcoinHoldings;
import com.ll.news.bitcoin.mapper.BitcoinEntitiesDetailMapper;
import com.ll.news.bitcoin.mapper.BitcoinEntitiesSummaryMapper;
import com.ll.news.bitcoin.mapper.BitcoinHoldingsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
@DS("analysis")
public class DailyCrawler {

    private static final String url = "https://treasuries.bitbo.io";

    private static final String special_entity_name = "SATO Technologies Corp.";

    @Autowired
    private BitcoinEntitiesSummaryMapper bitcoinEntitiesSummaryMapper;

    @Autowired
    private BitcoinHoldingsMapper bitcoinHoldingsMapper;

    @Autowired
    private BitcoinEntitiesDetailMapper bitcoinEntitiesDetailMapper;


    @Scheduled(cron = "0 30 8/6 * * ?")
//    @Scheduled(initialDelay = 1000, fixedDelay = 30_000)
    @Transactional(rollbackFor = Exception.class)
    public void bitcoinEntitiesCrawl() {
        try {
            // 解析HTML
            Document doc = Jsoup.connect(url).get();

            /* bitcoin_entities_summary */
            // 创建实体对象存储解析结果
            BitcoinEntitiesSummary entity = new BitcoinEntitiesSummary();
            // 提取桌面版表格数据
            Element table = doc.select("table.treasuries-index.treasuries-table.value-table").first();
            if (table != null) {
                boolean success = processSummary(table, entity);
                if (!success) {
                    return;
                }
            }

            if (entity.getId() == null) {
                log.error("BitcoinEntitiesSummary没有保存成功 直接返回");
                return;
            }
            /* bitcoin_holdings */
            // 获取表格
            Element h2Element = doc.select("h2.center-on-mobile:contains(Totals by Category)").first();
            List<BitcoinHoldings> list = processBitcoinHoldings(h2Element, entity);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            /* BitcoinEntitiesDetail  */
            for (BitcoinHoldings bitcoinHoldings : list) {
                EntityTypeEnum entityTypeEnum = EntityTypeEnum.enumMap.get(bitcoinHoldings.getCategory());
                if (entityTypeEnum == null) {
                    continue;
                }
                // 解析网页代码 提取数据
                processEntitiesDetail(doc, entityTypeEnum, bitcoinHoldings);
            }
        } catch (Exception e) {
            log.error("bitcoinEntitiesCrawl 本次不插入数据", e);
            throw new RuntimeException(e);
        }
    }

    private @NotNull List<BitcoinHoldings> processBitcoinHoldings(Element h2Element, BitcoinEntitiesSummary entity) {
        try {
            List<BitcoinHoldings> list = new ArrayList<>();
            if (h2Element != null) {
                // 找到h2后面的表格
                Element child = h2Element.nextElementSibling();
                if (child != null && child.hasClass("treasuries-table")) {
                    Elements rows = child.select("tbody tr");
                    // 遍历每一行并保存数据
                    for (Element row : rows) {
                        BitcoinHoldings holding = parseHoldingsRow(row, entity);
                        list.add(holding);
                    }
                } else {
                    log.error("未找到符合条件的表格:{}", entity);
                }
            } else {
                log.error("未找到标题'Totals by Category'");
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processEntitiesDetail(Document doc, EntityTypeEnum entityType, BitcoinHoldings bitcoinHoldings) {
        try {
            // 定位标题
            Element publicElement = doc.select("h2.center-on-mobile#" + entityType.getMark() + ":contains(" + entityType.getTitle() + ")").first();
            if (publicElement != null) {
                // 获取h2后的表格
                Element child = publicElement.nextElementSibling();
                if (entityType.getType() == 1) {
                    child = child.nextElementSibling();
                } else if (entityType.getType() == 5) {
                    child = child.nextElementSibling().nextElementSibling().nextElementSibling();
                }
                if (child != null && child.hasClass("treasuries-table")) {
                    Elements rows = child.select("tbody tr");
                    // 存储解析结果
                    List<BitcoinEntitiesDetail> entitiesDetails = new ArrayList<>();
                    // 遍历每一行（排除最后一行Totals）
                    for (int i = 0; i < rows.size() - 1; i++) {
                        Element row = rows.get(i);
                        BitcoinEntitiesDetail entitiesDetail = parseRow(row, entityType.getType(), bitcoinHoldings);
                        if (entitiesDetail != null) {
                            entitiesDetails.add(entitiesDetail);
                        }
                    }
                    // 保存到数据库
    //                saveToDatabase(entitiesDetails);
                } else {
                    log.error("未找到符合条件的表格'{}'", entityType.getName());
                }
            } else {
                log.error("未找到标题'{}'", entityType.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BitcoinEntitiesDetail parseRow(Element row, Integer entityType, BitcoinHoldings bitcoinHoldings) {
        BitcoinEntitiesDetail entitiesDetail = new BitcoinEntitiesDetail();

        try {
            entitiesDetail.setHoldingId(bitcoinHoldings.getId());
            // 实体名称
            String entityName = row.select("td.td-company a").text().trim();
            entitiesDetail.setEntityName(entityName);

            // 国家（从flag-icon的data-tooltip提取）
            String countryCode = row.select("td.td-location img.flag-icon")
                    .attr("data-tooltip");
            entitiesDetail.setCountry(countryCode);
            // 类型
            entitiesDetail.setEntityType(entityType);
            // 股票代码和交易所
            if (!row.select("td.td-symbol").text().isBlank()) {
                if (special_entity_name.equals(entityName)) {
                    entitiesDetail.setSymbolExchange(special_entity_name);
                } else {
                    entitiesDetail.setSymbolExchange(row.select("td.td-symbol").text().trim());
                }
            }
            // BTC数量（移除逗号）
            String btcText = row.select("td.td-company_btc").text().replace(",", "");
            entitiesDetail.setBtcAmount(new BigDecimal(btcText));

            // 百分比（移除%）
            String percentText = row.select("td.td-company_percent").text().replace("%", "");
            entitiesDetail.setPercentOf21m(new BigDecimal(percentText));

            // 最后更新日期（假设使用当前日期，可从其他来源获取）
            entitiesDetail.setLastUpdated(bitcoinHoldings.getLastUpdated());

            bitcoinEntitiesDetailMapper.insertBitcoinEntitiesDetail(entitiesDetail);
            return entitiesDetail;
        } catch (Exception e) {
            log.error("解析行数据失败: {}", e.getMessage());
            return null;
        }
    }

    private BitcoinHoldings parseHoldingsRow(Element row, BitcoinEntitiesSummary entity) {
        BitcoinHoldings holding = new BitcoinHoldings();
        holding.setSummaryId(entity.getId());
        // 提取类别
        holding.setCategory(row.select("td.td-symbol a").text());

        // 提取BTC数量（移除逗号）
        String btcText = row.select("td.td-company_btc").text().replace(",", "");
        holding.setBtcAmount(new BigDecimal(btcText));

        // 提取占21m的百分比（移除%）
        String percentText = row.select("td.td-company_percent").text().replace("%", "");
        holding.setPercentOf21m(new BigDecimal(percentText));
        holding.setLastUpdated(entity.getLastUpdated());
        bitcoinHoldingsMapper.insertBitcoinHoldings(holding);
        return holding;
    }

    private boolean processSummary(Element table, BitcoinEntitiesSummary entity) throws ParseException {
        Element row = table.select("tbody tr.top-table-data-row").first();
        //解析日期
        String dateStr = row.select("td.td-last-updated").text();
//        String dateStr = "March 28, 2025";
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse(dateStr);
        BitcoinEntitiesSummary query = new BitcoinEntitiesSummary();
        query.setLastUpdated(date);
        List<BitcoinEntitiesSummary> bitcoinEntitiesSummaries = bitcoinEntitiesSummaryMapper.selectBitcoinEntitiesSummaryList(query);
        if (CollectionUtils.isNotEmpty(bitcoinEntitiesSummaries)) {
            // date转为YYYY-MM-dd格式的时间
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
            log.info("=====================BitcoinEntitiesSummary数据:{}已存在，直接返回", dateString);
//            throw new RuntimeException("BitcoinEntitiesSummary有:" + dateString + "的数据，直接返回");
            return false;
        }
        entity.setEntityQuantity(Integer.parseInt(row.select("td.td-symbol").text()));
        entity.setTotalBtc(new BigDecimal(row.select("td.td-company_btc")
                .text().replace(",", "")));
        entity.setPercentOf21m(new BigDecimal(row.select("td.td-company_percent")
                .text().replace("%", "")));

        entity.setLastUpdated(new Date(date.getTime()));
        bitcoinEntitiesSummaryMapper.insertBitcoinEntitiesSummary(entity);
        log.info("bitcoin_entities_summary: " + entity);
        return true;
    }

//    private Date getYesterdayMidnight() {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        LocalDateTime midnight = yesterday.atStartOfDay();
//        ZonedDateTime zonedDateTime = midnight.atZone(ZoneId.of("UTC"));
//        return Date.from(zonedDateTime.toInstant());
//    }
}
