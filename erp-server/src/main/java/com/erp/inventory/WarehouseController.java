package com.erp.inventory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.inventory.entity.Warehouse;
import com.erp.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public Result<IPage<Warehouse>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword) {
        return Result.ok(warehouseService.pageWarehouses(page, size, keyword));
    }

    @GetMapping("/all")
    public Result<List<Warehouse>> all() {
        return Result.ok(warehouseService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<Void> create(@RequestBody Warehouse warehouse) {
        warehouseService.createWarehouse(warehouse);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouseService.updateWarehouse(id, warehouse);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.ok();
    }
}
