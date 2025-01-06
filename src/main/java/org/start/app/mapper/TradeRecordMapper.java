package org.start.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.start.app.entity.TradeRecord;
import org.start.app.model.TradeRecordQuery;

import java.util.List;

/**
 * 交易记录数据访问层接口
 */
@Mapper
public interface TradeRecordMapper {
    /**
     * 批量插入交易记录
     * @param records 交易记录列表
     * @return 插入成功的记录数
     */
    int batchInsert(List<TradeRecord> records);
    
    /**
     * 插入单条交易记录
     * @param record 交易记录
     * @return 插入成功返回1，否则返回0
     */
    int insert(TradeRecord record);
    
    /**
     * 根据ID删除交易记录
     * @param id 记录ID
     * @return 删除成功返回1，否则返回0
     */
    int deleteById(Long id);
    
    /**
     * 批量删除交易记录
     * @param ids ID列表
     * @return 删除的记录数
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 更新交易记录
     * @param record 交易记录
     * @return 更新成功返回1，否则返回0
     */
    int update(TradeRecord record);
    
    /**
     * 根据ID查询交易记录
     * @param id 记录ID
     * @return 交易记录
     */
    TradeRecord getById(Long id);
    
    /**
     * 根据交易单号查询记录
     * @param tradeNo 交易单号
     * @return 交易记录
     */
    TradeRecord getByTradeNo(String tradeNo);
    
    /**
     * 统计符合条件的记录总数
     * @param query 查询条件
     * @return 记录总数
     */
    long countByQuery(TradeRecordQuery query);
    
    /**
     * 分页查询交易记录
     * @param query 查询条件
     * @return 交易记录列表
     */
    List<TradeRecord> selectByQuery(TradeRecordQuery query);
} 