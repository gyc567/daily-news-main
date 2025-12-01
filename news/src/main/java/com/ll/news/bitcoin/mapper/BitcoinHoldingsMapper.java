package com.ll.news.bitcoin.mapper;

import com.ll.news.bitcoin.domain.BitcoinHoldings;

import java.util.List;

/**
 * 比特币国库券Mapper接口
 *
 * @author ruoyi
 * @date 2025-03-31
 */
public interface BitcoinHoldingsMapper
{
    /**
     * 查询比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 比特币国库券
     */
    public BitcoinHoldings selectBitcoinHoldingsById(Long id);

    /**
     * 查询比特币国库券列表
     *
     * @param bitcoinHoldings 比特币国库券
     * @return 比特币国库券集合
     */
    public List<BitcoinHoldings> selectBitcoinHoldingsList(BitcoinHoldings bitcoinHoldings);

    /**
     * 新增比特币国库券
     *
     * @param bitcoinHoldings 比特币国库券
     * @return 结果
     */
    public int insertBitcoinHoldings(BitcoinHoldings bitcoinHoldings);

    /**
     * 修改比特币国库券
     *
     * @param bitcoinHoldings 比特币国库券
     * @return 结果
     */
    public int updateBitcoinHoldings(BitcoinHoldings bitcoinHoldings);

    /**
     * 删除比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 结果
     */
    public int deleteBitcoinHoldingsById(Long id);

    /**
     * 批量删除比特币国库券
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBitcoinHoldingsByIds(Long[] ids);
}
