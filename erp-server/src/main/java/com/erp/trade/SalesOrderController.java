package com.erp.trade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.security.SecurityUtils;
import com.erp.trade.dto.SalesOrderRequest;
import com.erp.trade.entity.SalesOrder;
import com.erp.trade.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public Result<IPage<SalesOrder>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String keyword) {
        return Result.ok(salesOrderService.pageOrders(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> detail(@PathVariable Long id) {
        return Result.ok(salesOrderService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody SalesOrderRequest request) {
        return Result.ok(salesOrderService.createOrder(request, SecurityUtils.username()));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        salesOrderService.approve(id);
        return Result.ok();
    }

    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        salesOrderService.ship(id, SecurityUtils.username());
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        salesOrderService.cancel(id);
        return Result.ok();
    }
}
