package com.erp.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请填写数量")
    @Min(value = 1, message = "数量必须大于 0")
    private Integer qty;

    @NotNull(message = "请填写单价")
    @DecimalMin(value = "0", message = "单价不能为负数")
    private BigDecimal price;
}
