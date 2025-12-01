package com.ll.news.bitcoin.domain;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 比特币国库券对象 bitcoin_entities_detail
 *
 * @author ruoyi
 * @date 2025-03-31
 */
@Setter
@Getter
public class BitcoinEntitiesDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = -7141259629797391611L;
    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 关联 Bitcoin_Holdings 表
     */
    private Long holdingId;

    /**
     * 实体名称（如 USA、China 等）
     */
    private String entityName;

    /**
     * 国家
     */
    private String country;

    /**
     * 类型，0:unknown,1:ETFs,2:Countries,3:Public Companies,4:Private Companies,5:BTC Mining Companies,6:Defi
     */
    private Integer entityType;

    /**
     * 股票代码（如 MSTR:NADQ,RIOT:NADQ,0434.HK:HKEX）
     */
    private String symbolExchange;

    /**
     * 比特币数量
     */
    private BigDecimal btcAmount;

    /**
     * 占2100万比特币的比例
     */
    private BigDecimal percentOf21m;

    /**
     * 最后更新日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdated;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("holdingId", getHoldingId())
                .append("entityName", getEntityName())
                .append("country", getCountry())
                .append("entityType", getEntityType())
                .append("symbolExchange", getSymbolExchange())
                .append("btcAmount", getBtcAmount())
                .append("percentOf21m", getPercentOf21m())
                .append("lastUpdated", getLastUpdated())
                .append("createdAt", getCreatedAt())
                .append("updatedAt", getUpdatedAt())
                .toString();
    }
}
