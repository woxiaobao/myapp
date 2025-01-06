package org.start.app.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.start.app.entity.TradeRecord;
import org.start.app.mapper.TradeRecordMapper;

import java.util.ArrayList;
import java.util.List;

public class TradeRecordImportListener implements ReadListener<TradeRecord> {
    private static final Logger logger = LoggerFactory.getLogger(TradeRecordImportListener.class);
    private static final int BATCH_SIZE = 1000;

    private final List<TradeRecord> batch = new ArrayList<>(BATCH_SIZE);
    private final TradeRecordMapper tradeRecordMapper;

    public TradeRecordImportListener(TradeRecordMapper tradeRecordMapper) {
        this.tradeRecordMapper = tradeRecordMapper;
    }

    @Override
    public void invoke(TradeRecord data, AnalysisContext context) {
        batch.add(data);
        if (batch.size() >= BATCH_SIZE) {
            saveData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!batch.isEmpty()) {
            saveData();
        }
        logger.info("Excel导入完成");
    }

    private void saveData() {
        logger.info("正在保存 {} 条记录", batch.size());
        tradeRecordMapper.batchInsert(batch);
        batch.clear();
    }
} 