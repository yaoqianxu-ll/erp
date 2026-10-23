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
import com.erp.trade.dto.SalesOrderRequest;
import com.erp.trade.entity.SalesOrder;
import com.erp.trade.entity.SalesOrderItem;
import com.erp.trade.mapper.SalesOrderItemMapper;
import com.erp.trade.mapper.SalesOrderMapper;
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
public class SalesOrderService extends ServiceImpl<SalesOrderMapper, SalesOrder> {

    private final SalesOrderItemMapper itemMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockService stockService;

    public IPage<SalesOrder> pageOrders(long page, long size, String status, String keyword) {
        var wrapper = Wrappers.<SalesOrder>lambdaQuery();
        if (StringUtils.hasText(status)) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SalesOrder::getOrderNo, keyword)
                    .or().like(SalesOrder::getCustomer, keyword));
        }
        wrapper.orderByDesc(SalesOrder::getId);
        IPage<SalesOrder> result = page(new Page<>(page, Math.min(size, 100)), wrapper);
        fillWarehouseNames(result.getRecords());
        return result;
    }

    public SalesOrder detail(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException("销售订单不存在");
        }
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) {
            order.setWarehouseName(warehouse.getName());
        }
        List<SalesOrderItem> items = itemMapper.selectList(
                Wrappers.<SalesOrderItem>lambdaQuery().eq(SalesOrderItem::getOrderId, id));
        if (!items.isEmpty()) {
            Map<Long, Product> products = productMapper.selectBatchIds(
                            items.stream().map(SalesOrderItem::getProductId).distinct().toList())
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
    public Long createOrder(SalesOrderRequest request, String operator) {
        SalesOrder order = new SalesOrder();
        order.setOrderNo(generateNo());
        order.setCustomer(request.getCustomer());
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
            SalesOrderItem item = new SalesOrderItem();
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
                .eq(SalesOrder::getId, id)
                .eq(SalesOrder::getStatus, "PENDING")
                .set(SalesOrder::getStatus, "APPROVED")
                .update();
        if (!updated) {
            throw new BizException("当前状态不可审核");
        }
    }

    @Transactional
    public void ship(Long id, String operator) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException("销售订单不存在");
        }
        boolean updated = lambdaUpdate()
                .eq(SalesOrder::getId, id)
                .eq(SalesOrder::getStatus, "APPROVED")
                .set(SalesOrder::getStatus, "SHIPPED")
                .update();
        if (!updated) {
            throw new BizException("只有已审核的订单才能出库");
        }
        List<SalesOrderItem> items = itemMapper.selectList(
                Wrappers.<SalesOrderItem>lambdaQuery().eq(SalesOrderItem::getOrderId, id));
        for (SalesOrderItem item : items) {
            stockService.decrease(order.getWarehouseId(), item.getProductId(), item.getQty(),
                    "SALES", order.getOrderNo(), "销售出库", operator);
        }
    }

    public void cancel(Long id) {
        boolean updated = lambdaUpdate()
                .eq(SalesOrder::getId, id)
                .in(SalesOrder::getStatus, "PENDING", "APPROVED")
                .set(SalesOrder::getStatus, "CANCELLED")
                .update();
        if (!updated) {
            throw new BizException("当前状态不可取消");
        }
    }

    private void fillWarehouseNames(List<SalesOrder> orders) {
        List<Long> warehouseIds = orders.stream().map(SalesOrder::getWarehouseId).distinct().toList();
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
        return "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
