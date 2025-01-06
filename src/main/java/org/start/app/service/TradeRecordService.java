package org.start.app.service;

import com.alibaba.excel.EasyExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.start.app.entity.TradeRecord;
import org.start.app.excel.TradeRecordImportListener;
import org.start.app.mapper.TradeRecordMapper;
import org.start.app.model.TradeRecordQuery;
import org.start.app.util.ExcelDataGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeRecordService {
    private static final Logger logger = LoggerFactory.getLogger(TradeRecordService.class);

    private final TradeRecordMapper tradeRecordMapper;

    public TradeRecordService(TradeRecordMapper tradeRecordMapper) {
        this.tradeRecordMapper = tradeRecordMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), TradeRecord.class, 
                new TradeRecordImportListener(tradeRecordMapper))
                .sheet()
                .doRead();
    }

    public void generateTestExcel(String filePath) throws IOException {
        List<TradeRecord> records = ExcelDataGenerator.generateRecords(10000);
        
        EasyExcel.write(filePath, TradeRecord.class)
                .sheet("交易记录")
                .doWrite(records);
        
        logger.info("Generated test Excel file with 10000 records at: {}", filePath);
    }

    public TradeRecord getById(Long id) {
        return tradeRecordMapper.getById(id);
    }

    public TradeRecord getByTradeNo(String tradeNo) {
        return tradeRecordMapper.getByTradeNo(tradeNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insert(TradeRecord record) {
        return tradeRecordMapper.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TradeRecord record) {
        return tradeRecordMapper.update(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return tradeRecordMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        return tradeRecordMapper.deleteByIds(ids);
    }

    public Map<String, Object> queryPage(TradeRecordQuery query) {
        Map<String, Object> result = new HashMap<>();
        long total = tradeRecordMapper.countByQuery(query);
        List<TradeRecord> records = Collections.emptyList();
        if (total > 0) {
            records = tradeRecordMapper.selectByQuery(query);
        }
        result.put("total", total);
        result.put("records", records);
        return result;
    }
} 