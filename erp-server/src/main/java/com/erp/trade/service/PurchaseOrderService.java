package com.erp.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.inventory.entity.Warehouse;
import com.erp.inventory.mapper.WarehouseMapper;
import com.erp.inventory.service.StockService;
import com.erp.product.entity.Product;
import com.erp.product.mapper.ProductMapper;
import com.erp.trade.dto.OrderItemRequest;
import com.erp.trade.dto.PurchaseOrderRequest;
import com.erp.trade.entity.PurchaseOrder;
import com.erp.trade.entity.PurchaseOrderItem;
import com.erp.trade.mapper.PurchaseOrderItemMapper;
import com.erp.trade.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    private final PurchaseOrderItemMapper itemMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockService stockService;

    public IPage<PurchaseOrder> pageOrders(long page, long size, String status, String keyword) {
        var wrapper = Wrappers.<PurchaseOrder>lambdaQuery();
        if (StringUtils.hasText(status)) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PurchaseOrder::getOrderNo, keyword)
                    .or().like(PurchaseOrder::getSupplier, keyword));
        }
        wrapper.orderByDesc(PurchaseOrder::getId);
        IPage<PurchaseOrder> result = page(new Page<>(page, Math.min(size, 100)), wrapper);
        fillWarehouseNames(result.getRecords());
        return result;
    }

    public PurchaseOrder detail(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BizException("采购订单不存在");
        }
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) {
            order.setWarehouseName(warehouse.getName());
        }
        List<PurchaseOrderItem> items = itemMapper.selectList(
                Wrappers.<PurchaseOrderItem>lambdaQuery().eq(PurchaseOrderItem::getOrderId, id));
        if (!items.isEmpty()) {
            Map<Long, Product> products = productMapper.selectBatchIds(
                            items.stream().map(PurchaseOrderItem::getProductId).distinct().toList())
                    .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
            items.forEach(item -> {
                Product product = products.get(item.getProductId());
                if (product != null) {
                    item.setProductName(product.getName());
                    item.setProductCode(product.getCode());
                    item.setUnit(product.getUnit());
                }
            });
        }
        order.setItems(items);
        return order;
    }

    @Transactional
    public Long createOrder(PurchaseOrderRequest request, String operator) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateNo());
        order.setSupplier(request.getSupplier());
        order.setWarehouseId(request.getWarehouseId());
        order.setStatus("PENDING");
        order.setRemark(request.getRemark());
        order.setCreateBy(operator);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            if (productMapper.selectById(item.getProductId()) == null) {
                throw new BizException("所选商品不存在");
            }
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
        }
        order.setTotalAmount(total);
        save(order);
        for (OrderItemRequest req : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(req.getProductId());
            item.setQty(req.getQty());
            item.setPrice(req.getPrice());
            item.setAmount(req.getPrice().multiply(BigDecimal.valueOf(req.getQty())));
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Transactional
    public void approve(Long id) {
        boolean updated = lambdaUpdate()
                .eq(PurchaseOrder::getId, id)
                .eq(PurchaseOrder::getStatus, "PENDING")
                .set(PurchaseOrder::getStatus, "APPROVED")
                .update();
        if (!updated) {
            throw new BizException("当前状态不可审核");
        }
    }

    @Transactional
    public void receive(Long id, String operator) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BizException("采购订单不存在");
        }
        boolean updated = lambdaUpdate()
                .eq(PurchaseOrder::getId, id)
                .eq(PurchaseOrder::getStatus, "APPROVED")
                .set(PurchaseOrder::getStatus, "RECEIVED")
                .update();
        if (!updated) {
            throw new BizException("只有已审核的订单才能入库");
        }
        List<PurchaseOrderItem> items = itemMapper.selectList(
                Wrappers.<PurchaseOrderItem>lambdaQuery().eq(PurchaseOrderItem::getOrderId, id));
        for (PurchaseOrderItem item : items) {
            stockService.increase(order.getWarehouseId(), item.getProductId(), item.getQty(),
                    "PURCHASE", order.getOrderNo(), "采购入库", operator);
        }
    }

    public void cancel(Long id) {
        boolean updated = lambdaUpdate()
                .eq(PurchaseOrder::getId, id)
                .in(PurchaseOrder::getStatus, "PENDING", "APPROVED")
                .set(PurchaseOrder::getStatus, "CANCELLED")
                .update();
        if (!updated) {
            throw new BizException("当前状态不可取消");
        }
    }

    private void fillWarehouseNames(List<PurchaseOrder> orders) {
        List<Long> warehouseIds = orders.stream().map(PurchaseOrder::getWarehouseId).distinct().toList();
        if (warehouseIds.isEmpty()) {
            return;
        }
        Map<Long, Warehouse> warehouses = warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        orders.forEach(order -> {
            Warehouse warehouse = warehouses.get(order.getWarehouseId());
            if (warehouse != null) {
                order.setWarehouseName(warehouse.getName());
            }
        });
    }

    private String generateNo() {
        return "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
