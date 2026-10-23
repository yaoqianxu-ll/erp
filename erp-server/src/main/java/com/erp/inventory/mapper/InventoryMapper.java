package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Insert("INSERT INTO erp_inventory(warehouse_id, product_id, qty, create_time, update_time) " +
            "VALUES(#{warehouseId}, #{productId}, #{qty}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE qty = qty + #{qty}, update_time = NOW()")
    int increase(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId, @Param("qty") Integer qty);

    @Update("UPDATE erp_inventory SET qty = qty - #{qty}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} AND qty >= #{qty}")
    int decrease(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId, @Param("qty") Integer qty);

    @Select("SELECT IFNULL(SUM(i.qty * p.purchase_price), 0) FROM erp_inventory i " +
            "INNER JOIN erp_product p ON p.id = i.product_id AND p.deleted = 0")
    BigDecimal totalValue();

    @Select("SELECT i.id, i.warehouse_id, i.product_id, i.qty, i.update_time, " +
            "p.name AS product_name, p.code AS product_code, p.unit, p.safety_stock, w.name AS warehouse_name " +
            "FROM erp_inventory i " +
            "INNER JOIN erp_product p ON p.id = i.product_id AND p.deleted = 0 " +
            "INNER JOIN erp_warehouse w ON w.id = i.warehouse_id " +
            "WHERE i.qty <= p.safety_stock ORDER BY (p.safety_stock - i.qty) DESC LIMIT 8")
    List<Inventory> selectLowStock();
}
