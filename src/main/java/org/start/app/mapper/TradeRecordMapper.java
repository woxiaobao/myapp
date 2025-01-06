package org.start.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.start.app.entity.TradeRecord;
import org.start.app.model.TradeRecordQuery;

import java.util.List;

@Mapper
public interface TradeRecordMapper {
    // 批量插入
    int batchInsert(List<TradeRecord> records);
    
    // 插入单条记录
    int insert(TradeRecord record);
    
    // 根据ID删除
    int deleteById(Long id);
    
    // 根据ID批量删除
    int deleteByIds(@Param("ids") List<Long> ids);
    
    // 更新记录
    int update(TradeRecord record);
    
    // 根据ID查询
    TradeRecord getById(Long id);
    
    // 根据交易单号查询
    TradeRecord getByTradeNo(String tradeNo);
    
    // 条件查询总数
    long countByQuery(TradeRecordQuery query);
    
    // 条件分页查询
    List<TradeRecord> selectByQuery(TradeRecordQuery query);
} 