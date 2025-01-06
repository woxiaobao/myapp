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

/**
 * 交易记录服务层
 * 处理业务逻辑
 */
@Service
public class TradeRecordService {
    private static final Logger logger = LoggerFactory.getLogger(TradeRecordService.class);

    /** 数据访问层对象 */
    private final TradeRecordMapper tradeRecordMapper;

    /**
     * 构造方法，注入依赖
     * @param tradeRecordMapper Mapper对象
     */
    public TradeRecordService(TradeRecordMapper tradeRecordMapper) {
        this.tradeRecordMapper = tradeRecordMapper;
    }

    /**
     * 导入Excel文件
     * @param file Excel文件
     * @throws IOException IO异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), TradeRecord.class, 
                new TradeRecordImportListener(tradeRecordMapper))
                .sheet()
                .doRead();
    }

    /**
     * 生成测试用Excel文件
     * @param filePath 文件保存路径
     * @throws IOException IO异常
     */
    public void generateTestExcel(String filePath) throws IOException {
        List<TradeRecord> records = ExcelDataGenerator.generateRecords(10000);
        
        EasyExcel.write(filePath, TradeRecord.class)
                .sheet("交易记录")
                .doWrite(records);
        
        logger.info("Generated test Excel file with 10000 records at: {}", filePath);
    }

    /**
     * 根据ID查询记录
     */
    public TradeRecord getById(Long id) {
        return tradeRecordMapper.getById(id);
    }

    /**
     * 根据交易单号查询记录
     */
    public TradeRecord getByTradeNo(String tradeNo) {
        return tradeRecordMapper.getByTradeNo(tradeNo);
    }

    /**
     * 新增记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int insert(TradeRecord record) {
        return tradeRecordMapper.insert(record);
    }

    /**
     * 更新记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(TradeRecord record) {
        return tradeRecordMapper.update(record);
    }

    /**
     * 删除记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return tradeRecordMapper.deleteById(id);
    }

    /**
     * 批量删除记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        return tradeRecordMapper.deleteByIds(ids);
    }

    /**
     * 分页查询
     * @param query 查询条件
     * @return 包含总数和记录列表的Map
     */
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