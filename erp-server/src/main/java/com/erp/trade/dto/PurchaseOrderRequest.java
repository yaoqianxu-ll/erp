package com.erp.trade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotBlank(message = "请填写供应商")
    private String supplier;

    @NotNull(message = "请选择入库仓库")
    private Long warehouseId;

    private String remark;

    @Valid
    @NotEmpty(message = "请至少添加一条明细")
    private List<OrderItemRequest> items;
}
