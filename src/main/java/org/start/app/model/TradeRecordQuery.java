package org.start.app.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeRecordQuery {
    private String tradeType;
    private String counterparty;
    private String direction;
    private String status;
    private Date startTime;
    private Date endTime;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String paymentMethod;
    private String tradeNo;
    private String merchantOrderNo;
    
    // 分页参数
    private Integer pageNum = 1;
    private Integer pageSize = 10;
} 