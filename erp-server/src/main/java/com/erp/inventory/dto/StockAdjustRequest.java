package com.erp.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StockAdjustRequest {

    @NotNull(message = "请选择仓库")
    private Long warehouseId;

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请填写数量")
    @Min(value = 1, message = "数量必须大于 0")
    private Integer qty;

    @Pattern(regexp = "IN|OUT", message = "类型必须为 IN 或 OUT")
    private String type;

    private String remark;
}
