package com.erp.dashboard;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.common.Result;
import com.erp.inventory.entity.Inventory;
import com.erp.inventory.mapper.InventoryMapper;
import com.erp.inventory.mapper.WarehouseMapper;
import com.erp.inventory.service.InventoryService;
import com.erp.product.entity.Product;
import com.erp.product.mapper.ProductMapper;
import com.erp.trade.entity.PurchaseOrder;
import com.erp.trade.entity.SalesOrder;
import com.erp.trade.mapper.PurchaseOrderMapper;
import com.erp.trade.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productCount", productMapper.selectCount(
                Wrappers.<Product>lambdaQuery().eq(Product::getStatus, 1)));
        result.put("warehouseCount", warehouseMapper.selectCount(null));

        List<Inventory> inventories = inventoryMapper.selectList(null);
        int totalQty = inventories.stream().mapToInt(Inventory::getQty).sum();
        result.put("totalQty", totalQty);

        BigDecimal totalValue = inventoryMapper.totalValue();
        result.put("totalValue", totalValue == null ? BigDecimal.ZERO : totalValue);

        result.put("purchasePending", purchaseOrderMapper.selectCount(
                Wrappers.<PurchaseOrder>lambdaQuery().eq(PurchaseOrder::getStatus, "PENDING")));
        result.put("salesPending", salesOrderMapper.selectCount(
                Wrappers.<SalesOrder>lambdaQuery().eq(SalesOrder::getStatus, "PENDING")));

        List<Inventory> lowStock = inventoryService.lowStock();
        result.put("lowStock", lowStock);
        result.put("lowStockCount", lowStock.size());
        result.put("recentRecords", inventoryService.pageRecords(1, 8, null, null).getRecords());
        return Result.ok(result);
    }
}
