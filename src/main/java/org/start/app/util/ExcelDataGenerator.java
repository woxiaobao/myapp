package org.start.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.start.app.entity.TradeRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 测试数据生成器
 * 用于生成测试用的交易记录数据
 */
public class ExcelDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataGenerator.class);
    
    /** 交易类型列表 */
    private static final String[] TRADE_TYPES = {"购物", "转账", "充值", "提现", "退款"};
    /** 交易对方列表 */
    private static final String[] COUNTERPARTIES = {"张三", "李四", "王五", "赵六", "商城", "超市"};
    /** 商品列表 */
    private static final String[] PRODUCTS = {"手机", "电脑", "食品", "服装", "日用品", "电器"};
    /** 支付方式列表 */
    private static final String[] PAYMENT_METHODS = {"支付宝", "微信", "银行卡", "现金"};
    /** 交易状态列表 */
    private static final String[] STATUSES = {"成功", "失败", "处理中", "已退款"};
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 生成指定数量的测试数据
     * @param count 需要生成的记录数量
     * @return 交易记录列表
     */
    public static List<TradeRecord> generateRecords(int count) {
        List<TradeRecord> records = new ArrayList<>(count);
        Set<String> usedTradeNos = new HashSet<>();
        Random random = new Random();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1); // 从一年前开始生成数据
        
        for (int i = 0; i < count; i++) {
            TradeRecord record = new TradeRecord();
            
            // 生成递增的时间
            calendar.add(Calendar.MINUTE, random.nextInt(10));
            record.setTradeTime(calendar.getTime());
            
            // 随机选择交易类型等信息
            record.setTradeType(TRADE_TYPES[random.nextInt(TRADE_TYPES.length)]);
            record.setCounterparty(COUNTERPARTIES[random.nextInt(COUNTERPARTIES.length)]);
            record.setProduct(PRODUCTS[random.nextInt(PRODUCTS.length)]);
            record.setDirection(random.nextBoolean() ? "收" : "支");
            
            // 生成随机金额
            BigDecimal amount = new BigDecimal(random.nextDouble() * 9900 + 100)
                    .setScale(2, RoundingMode.HALF_UP);
            record.setAmount(amount);
            
            record.setPaymentMethod(PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)]);
            record.setStatus(STATUSES[random.nextInt(STATUSES.length)]);
            
            // 生成唯一的交易单号
            String tradeNo;
            do {
                tradeNo = String.format("T%s%04d", 
                        System.currentTimeMillis(), random.nextInt(10000));
            } while (!usedTradeNos.add(tradeNo));
            record.setTradeNo(tradeNo);
            
            // 生成商户单号
            record.setMerchantOrderNo("M" + System.nanoTime() + random.nextInt(1000));
            record.setRemarks("测试数据-" + (i + 1));
            
            // 在生成数据时格式化日期输出
            logger.info("Generated record with time: {}", DATE_FORMAT.format(record.getTradeTime()));
            
            records.add(record);
        }
        
        return records;
    }
} 