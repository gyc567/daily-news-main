package com.ll.news.bitcoin.mapper;

import com.ll.news.bitcoin.domain.BitcoinEntitiesDetail;

import java.util.List;

/**
 * 比特币国库券Mapper接口
 *
 * @author ruoyi
 * @date 2025-03-31
 */
public interface BitcoinEntitiesDetailMapper
{
    /**
     * 查询比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 比特币国库券
     */
    public BitcoinEntitiesDetail selectBitcoinEntitiesDetailById(Long id);

    /**
     * 查询比特币国库券列表
     *
     * @param bitcoinEntitiesDetail 比特币国库券
     * @return 比特币国库券集合
     */
    public List<BitcoinEntitiesDetail> selectBitcoinEntitiesDetailList(BitcoinEntitiesDetail bitcoinEntitiesDetail);

    /**
     * 新增比特币国库券
     *
     * @param bitcoinEntitiesDetail 比特币国库券
     * @return 结果
     */
    public int insertBitcoinEntitiesDetail(BitcoinEntitiesDetail bitcoinEntitiesDetail);

    /**
     * 修改比特币国库券
     *
     * @param bitcoinEntitiesDetail 比特币国库券
     * @return 结果
     */
    public int updateBitcoinEntitiesDetail(BitcoinEntitiesDetail bitcoinEntitiesDetail);

    /**
     * 删除比特币国库券
     *
     * @param id 比特币国库券主键
     * @return 结果
     */
    public int deleteBitcoinEntitiesDetailById(Long id);

    /**
     * 批量删除比特币国库券
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBitcoinEntitiesDetailByIds(Long[] ids);
}
