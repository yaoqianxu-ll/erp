package com.erp.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("erp_sales_order")
public class SalesOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String customer;
    private Long warehouseId;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
    private String createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private List<SalesOrderItem> items = new ArrayList<>();
}
