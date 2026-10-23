package com.erp.inventory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.inventory.dto.StockAdjustRequest;
import com.erp.inventory.entity.Inventory;
import com.erp.inventory.entity.StockRecord;
import com.erp.inventory.service.InventoryService;
import com.erp.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<IPage<Inventory>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) Long warehouseId,
                                         @RequestParam(required = false) String keyword) {
        return Result.ok(inventoryService.pageInventory(page, size, warehouseId, keyword));
    }

    @GetMapping("/records")
    public Result<IPage<StockRecord>> records(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String type,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(inventoryService.pageRecords(page, size, type, keyword));
    }

    @GetMapping("/low-stock")
    public Result<List<Inventory>> lowStock() {
        return Result.ok(inventoryService.lowStock());
    }

    @PostMapping("/adjust")
    public Result<Void> adjust(@Valid @RequestBody StockAdjustRequest request) {
        inventoryService.adjust(request, SecurityUtils.username());
        return Result.ok();
    }
}
