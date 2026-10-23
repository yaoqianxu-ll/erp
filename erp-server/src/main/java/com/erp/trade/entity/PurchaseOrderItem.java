package com.erp.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("erp_purchase_order_item")
public class PurchaseOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal amount;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
    @TableField(exist = false)
    private String unit;
}
