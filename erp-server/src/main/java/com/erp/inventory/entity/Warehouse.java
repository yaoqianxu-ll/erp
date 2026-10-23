package com.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("erp_warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String address;
    private String manager;
    private String phone;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
