package com.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_stock_record")
public class StockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private Long warehouseId;
    private Long productId;
    private Integer qty;
    private String bizType;
    private String bizNo;
    private String remark;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private String warehouseName;
}
