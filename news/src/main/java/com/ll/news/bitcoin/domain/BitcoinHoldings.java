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
 * 比特币国库券对象 bitcoin_holdings
 *
 * @author ruoyi
 * @date 2025-03-31
 */
@Setter
@Getter
public class BitcoinHoldings implements Serializable {

    @Serial
    private static final long serialVersionUID = -7539049674459855266L;
    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 关联 bitcoin_entities_summary 表
     */
    private Long summaryId;

    /**
     * 类别（如 ETFs、Countries等)
     */
    private String category;

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
                .append("summaryId", getSummaryId())
                .append("category", getCategory())
                .append("btcAmount", getBtcAmount())
                .append("percentOf21m", getPercentOf21m())
                .append("lastUpdated", getLastUpdated())
                .append("createdAt", getCreatedAt())
                .append("updatedAt", getUpdatedAt())
                .toString();
    }
}
