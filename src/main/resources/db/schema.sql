CREATE TABLE IF NOT EXISTS `trade_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trade_time` datetime NOT NULL COMMENT '交易时间',
  `trade_type` varchar(50) NOT NULL COMMENT '交易类型',
  `counterparty` varchar(100) NOT NULL COMMENT '交易对方',
  `product` varchar(200) NOT NULL COMMENT '商品',
  `direction` varchar(10) NOT NULL COMMENT '收/支',
  `amount` decimal(10,2) NOT NULL COMMENT '金额(元)',
  `payment_method` varchar(50) NOT NULL COMMENT '支付方式',
  `status` varchar(50) NOT NULL COMMENT '当前状态',
  `trade_no` varchar(100) NOT NULL COMMENT '交易单号',
  `merchant_order_no` varchar(100) COMMENT '商户单号',
  `remarks` varchar(500) COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_no` (`trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表'; 