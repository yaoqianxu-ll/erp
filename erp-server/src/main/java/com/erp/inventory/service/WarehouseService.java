package com.erp.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.inventory.entity.Inventory;
import com.erp.inventory.entity.Warehouse;
import com.erp.inventory.mapper.InventoryMapper;
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
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

    private final InventoryMapper inventoryMapper;

    public IPage<Warehouse> pageWarehouses(long page, long size, String keyword) {
        var wrapper = Wrappers.<Warehouse>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Warehouse::getName, keyword).or().like(Warehouse::getCode, keyword));
        }
        wrapper.orderByAsc(Warehouse::getId);
        return page(new Page<>(page, Math.min(size, 100)), wrapper);
    }

    public List<Warehouse> listAll() {
        return list(Wrappers.<Warehouse>lambdaQuery().eq(Warehouse::getStatus, 1).orderByAsc(Warehouse::getId));
    }

    public void createWarehouse(Warehouse warehouse) {
        checkCodeUnique(warehouse.getCode(), null);
        warehouse.setId(null);
        warehouse.setStatus(warehouse.getStatus() == null ? 1 : warehouse.getStatus());
        save(warehouse);
    }

    public void updateWarehouse(Long id, Warehouse warehouse) {
        checkCodeUnique(warehouse.getCode(), id);
        warehouse.setId(id);
        updateById(warehouse);
    }

    public void deleteWarehouse(Long id) {
        Long count = inventoryMapper.selectCount(
                Wrappers.<Inventory>lambdaQuery().eq(Inventory::getWarehouseId, id));
        if (count != null && count > 0) {
            throw new BizException("该仓库已有库存数据，无法删除");
        }
        removeById(id);
    }

    private void checkCodeUnique(String code, Long excludeId) {
        var wrapper = Wrappers.<Warehouse>lambdaQuery().eq(Warehouse::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Warehouse::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BizException("仓库编码已存在");
        }
    }
}
