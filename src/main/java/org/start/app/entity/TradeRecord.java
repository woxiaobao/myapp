package org.start.app.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.start.app.excel.DateConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录实体类
 * 用于存储交易相关的所有信息
 */
@Data
public class TradeRecord {
    /** 主键ID */
    private Long id;

    /** Excel中的"交易时间"列 */
    @ExcelProperty(value = "交易时间", converter = DateConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date tradeTime;

    /** Excel中的"交易类型"列 */
    @ExcelProperty("交易类型")
    private String tradeType;

    /** Excel中的"交易对方"列 */
    @ExcelProperty("交易对方")
    private String counterparty;

    /** Excel中的"商品"列 */
    @ExcelProperty("商品")
    private String product;

    /** Excel中的"收/支"列 */
    @ExcelProperty("收/支")
    private String direction;

    /** Excel中的"金额(元)"列 */
    @ExcelProperty("金额(元)")
    private BigDecimal amount;

    /** Excel中的"支付方式"列 */
    @ExcelProperty("支付方式")
    private String paymentMethod;

    /** Excel中的"当前状态"列 */
    @ExcelProperty("当前状态")
    private String status;

    /** Excel中的"交易单号"列 */
    @ExcelProperty("交易单号")
    private String tradeNo;

    /** Excel中的"商户单号"列 */
    @ExcelProperty("商户单号")
    private String merchantOrderNo;

    /** Excel中的"备注"列 */
    @ExcelProperty("备注")
    private String remarks;

    /** 记录创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
} 