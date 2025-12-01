package com.ll.news.bitcoin.mapper;

import com.ll.news.bitcoin.domain.BitcoinEntitiesSummary;

import java.util.List;

/**
 * 比特币国库券Mapper接口
 *
 * @author ruoyi
 * @date 2025-03-31
 */
public interface BitcoinEntitiesSummaryMapper {
    /**
     * 查询比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 比特币国库券
     */
    public BitcoinEntitiesSummary selectBitcoinEntitiesSummaryById(Long id);

    /**
     * 查询比特币国库券列表
     *
     * @param bitcoinEntitiesSummary 比特币国库券
     * @return 比特币国库券集合
     */
    public List<BitcoinEntitiesSummary> selectBitcoinEntitiesSummaryList(BitcoinEntitiesSummary bitcoinEntitiesSummary);

    /**
     * 新增比特币国库券
     *
     * @param bitcoinEntitiesSummary 比特币国库券
     * @return 结果
     */
    public int insertBitcoinEntitiesSummary(BitcoinEntitiesSummary bitcoinEntitiesSummary);

    /**
     * 修改比特币国库券
     *
     * @param bitcoinEntitiesSummary 比特币国库券
     * @return 结果
     */
    public int updateBitcoinEntitiesSummary(BitcoinEntitiesSummary bitcoinEntitiesSummary);

    /**
     * 删除比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 结果
     */
    public int deleteBitcoinEntitiesSummaryById(Long id);

    /**
     * 批量删除比特币国库券
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBitcoinEntitiesSummaryByIds(Long[] ids);
}
