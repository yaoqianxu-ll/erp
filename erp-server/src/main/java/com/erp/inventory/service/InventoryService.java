package com.erp.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.inventory.dto.StockAdjustRequest;
import com.erp.inventory.entity.Inventory;
import com.erp.inventory.entity.StockRecord;
import com.erp.inventory.entity.Warehouse;
import com.erp.inventory.mapper.InventoryMapper;
import com.erp.inventory.mapper.StockRecordMapper;
import com.erp.inventory.mapper.WarehouseMapper;
import com.erp.product.entity.Product;
import com.erp.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final StockRecordMapper recordMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockService stockService;

    public IPage<Inventory> pageInventory(long page, long size, Long warehouseId, String keyword) {
        var wrapper = Wrappers.<Inventory>lambdaQuery();
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }
        if (StringUtils.hasText(keyword)) {
            List<Long> productIds = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                            .like(Product::getName, keyword).or().like(Product::getCode, keyword))
                    .stream().map(Product::getId).toList();
            if (productIds.isEmpty()) {
                Page<Inventory> empty = new Page<>(page, Math.min(size, 100));
                empty.setRecords(List.of());
                empty.setTotal(0);
                return empty;
            }
            wrapper.in(Inventory::getProductId, productIds);
        }
        wrapper.orderByAsc(Inventory::getWarehouseId).orderByAsc(Inventory::getProductId);
        IPage<Inventory> result = inventoryMapper.selectPage(new Page<>(page, Math.min(size, 100)), wrapper);
        fillNames(result.getRecords());
        return result;
    }

    public IPage<StockRecord> pageRecords(long page, long size, String type, String keyword) {
        var wrapper = Wrappers.<StockRecord>lambdaQuery();
        if (StringUtils.hasText(type)) {
            wrapper.eq(StockRecord::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            List<Long> productIds = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                            .like(Product::getName, keyword).or().like(Product::getCode, keyword))
                    .stream().map(Product::getId).toList();
            wrapper.and(w -> {
                w.like(StockRecord::getBizNo, keyword);
                if (!productIds.isEmpty()) {
                    w.or().in(StockRecord::getProductId, productIds);
                }
            });
        }
        wrapper.orderByDesc(StockRecord::getCreateTime).orderByDesc(StockRecord::getId);
        IPage<StockRecord> result = recordMapper.selectPage(new Page<>(page, Math.min(size, 100)), wrapper);
        fillRecordNames(result.getRecords());
        return result;
    }

    public List<Inventory> lowStock() {
        return inventoryMapper.selectLowStock();
    }

    public void adjust(StockAdjustRequest request, String operator) {
        stockService.adjust(request, operator);
    }

    private void fillNames(List<Inventory> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> productIds = records.stream().map(Inventory::getProductId).distinct().toList();
        List<Long> warehouseIds = records.stream().map(Inventory::getWarehouseId).distinct().toList();
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Warehouse> warehouses = warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        records.forEach(inv -> {
            Product product = products.get(inv.getProductId());
            if (product != null) {
                inv.setProductName(product.getName());
                inv.setProductCode(product.getCode());
                inv.setUnit(product.getUnit());
                inv.setSafetyStock(product.getSafetyStock());
            }
            Warehouse warehouse = warehouses.get(inv.getWarehouseId());
            if (warehouse != null) {
                inv.setWarehouseName(warehouse.getName());
            }
        });
    }

    private void fillRecordNames(List<StockRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> productIds = records.stream().map(StockRecord::getProductId).distinct().toList();
        List<Long> warehouseIds = records.stream().map(StockRecord::getWarehouseId).distinct().toList();
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Warehouse> warehouses = warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        records.forEach(record -> {
            Product product = products.get(record.getProductId());
            if (product != null) {
                record.setProductName(product.getName());
                record.setProductCode(product.getCode());
                record.setUnit(product.getUnit());
            }
            Warehouse warehouse = warehouses.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseName(warehouse.getName());
            }
        });
    }
}
