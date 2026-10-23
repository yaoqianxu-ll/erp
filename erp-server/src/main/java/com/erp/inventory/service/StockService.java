package com.erp.inventory.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.common.BizException;
import com.erp.inventory.dto.StockAdjustRequest;
import com.erp.inventory.entity.StockRecord;
import com.erp.inventory.mapper.InventoryMapper;
import com.erp.inventory.mapper.StockRecordMapper;
import com.erp.product.entity.Product;
import com.erp.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final InventoryMapper inventoryMapper;
    private final StockRecordMapper recordMapper;
    private final ProductMapper productMapper;

    @Transactional
    public void increase(Long warehouseId, Long productId, int qty,
                         String bizType, String bizNo, String remark, String operator) {
        inventoryMapper.increase(warehouseId, productId, qty);
        saveRecord("IN", warehouseId, productId, qty, bizType, bizNo, remark, operator);
    }

    @Transactional
    public void decrease(Long warehouseId, Long productId, int qty,
                         String bizType, String bizNo, String remark, String operator) {
        int rows = inventoryMapper.decrease(warehouseId, productId, qty);
        if (rows == 0) {
            Product product = productMapper.selectById(productId);
            String name = product == null ? "商品" : product.getName();
            throw new BizException("[" + name + "] 库存不足，无法出库");
        }
        saveRecord("OUT", warehouseId, productId, qty, bizType, bizNo, remark, operator);
    }

    @Transactional
    public void adjust(StockAdjustRequest request, String operator) {
        if (productMapper.selectById(request.getProductId()) == null) {
            throw new BizException("商品不存在");
        }
        if (!"IN".equals(request.getType()) && !"OUT".equals(request.getType())) {
            throw new BizException("请选择盘点类型");
        }
        if ("IN".equals(request.getType())) {
            increase(request.getWarehouseId(), request.getProductId(), request.getQty(),
                    "ADJUST", null, request.getRemark(), operator);
        } else {
            decrease(request.getWarehouseId(), request.getProductId(), request.getQty(),
                    "ADJUST", null, request.getRemark(), operator);
        }
    }

    private void saveRecord(String type, Long warehouseId, Long productId, int qty,
                            String bizType, String bizNo, String remark, String operator) {
        StockRecord record = new StockRecord();
        record.setType(type);
        record.setWarehouseId(warehouseId);
        record.setProductId(productId);
        record.setQty(qty);
        record.setBizType(bizType);
        record.setBizNo(bizNo);
        record.setRemark(remark);
        record.setCreateBy(operator);
        recordMapper.insert(record);
    }
}
