package org.start.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录查询参数类
 * 用于封装查询条件
 */
@Data
public class TradeRecordQuery {
    /** 交易类型 */
    private String tradeType;
    
    /** 交易对方 */
    private String counterparty;
    
    /** 收支方向 */
    private String direction;
    
    /** 交易状态 */
    private String status;
    
    /** 交易开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    /** 交易结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    /** 最小金额 */
    private BigDecimal minAmount;
    
    /** 最大金额 */
    private BigDecimal maxAmount;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 交易单号 */
    private String tradeNo;
    
    /** 商户单号 */
    private String merchantOrderNo;
    
    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;
    
    /** 每页记录数，默认10条 */
    private Integer pageSize = 10;

    /**
     * 获取分页偏移量
     * @return 偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
} 