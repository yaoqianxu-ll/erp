package com.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private Long productId;
    private Integer qty;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productCode;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private Integer safetyStock;
}
