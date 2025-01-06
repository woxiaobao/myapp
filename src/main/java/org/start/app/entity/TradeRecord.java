package org.start.app.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeRecord {
    private Long id;

    @ExcelProperty("交易时间")
    private Date tradeTime;

    @ExcelProperty("交易类型")
    private String tradeType;

    @ExcelProperty("交易对方")
    private String counterparty;

    @ExcelProperty("商品")
    private String product;

    @ExcelProperty("收/支")
    private String direction;

    @ExcelProperty("金额(元)")
    private BigDecimal amount;

    @ExcelProperty("支付方式")
    private String paymentMethod;

    @ExcelProperty("当前状态")
    private String status;

    @ExcelProperty("交易单号")
    private String tradeNo;

    @ExcelProperty("商户单号")
    private String merchantOrderNo;

    @ExcelProperty("备注")
    private String remarks;

    private Date createTime;
} 