package com.erp.trade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesOrderRequest {

    @NotBlank(message = "请填写客户名称")
    private String customer;

    @NotNull(message = "请选择出库仓库")
    private Long warehouseId;

    private String remark;

    @Valid
    @NotEmpty(message = "请至少添加一条明细")
    private List<OrderItemRequest> items;
}
