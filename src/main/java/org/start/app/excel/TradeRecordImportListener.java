package org.start.app.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.start.app.entity.TradeRecord;
import org.start.app.mapper.TradeRecordMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入监听器
 * 用于处理Excel文件的读取和数据保存
 */
public class TradeRecordImportListener implements ReadListener<TradeRecord> {
    private static final Logger logger = LoggerFactory.getLogger(TradeRecordImportListener.class);
    /** 每批处理的数据量 */
    private static final int BATCH_SIZE = 1000;

    /** 暂存的数据列表 */
    private final List<TradeRecord> batch = new ArrayList<>(BATCH_SIZE);
    /** 数据访问层对象 */
    private final TradeRecordMapper tradeRecordMapper;

    /**
     * 构造方法
     * @param tradeRecordMapper Mapper对象
     */
    public TradeRecordImportListener(TradeRecordMapper tradeRecordMapper) {
        this.tradeRecordMapper = tradeRecordMapper;
    }

    /**
     * 处理每一行数据
     */
    @Override
    public void invoke(TradeRecord data, AnalysisContext context) {
        batch.add(data);
        if (batch.size() >= BATCH_SIZE) {
            saveData();
        }
    }

    /**
     * 所有数据解析完成后的操作
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!batch.isEmpty()) {
            saveData();
        }
        logger.info("Excel导入完成");
    }

    /**
     * 保存数据到数据库
     */
    private void saveData() {
        logger.info("正在保存 {} 条记录", batch.size());
        tradeRecordMapper.batchInsert(batch);
        batch.clear();
    }
} 