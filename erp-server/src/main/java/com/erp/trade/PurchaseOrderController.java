package com.erp.trade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.security.SecurityUtils;
import com.erp.trade.dto.PurchaseOrderRequest;
import com.erp.trade.entity.PurchaseOrder;
import com.erp.trade.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public Result<IPage<PurchaseOrder>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword) {
        return Result.ok(purchaseOrderService.pageOrders(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        return Result.ok(purchaseOrderService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return Result.ok(purchaseOrderService.createOrder(request, SecurityUtils.username()));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        purchaseOrderService.approve(id);
        return Result.ok();
    }

    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        purchaseOrderService.receive(id, SecurityUtils.username());
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return Result.ok();
    }
}
