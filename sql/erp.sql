-- =============================================================
-- 简易 ERP 系统 数据库脚本 (MySQL 8.0+)
-- 账号 admin / 123456
-- =============================================================
CREATE DATABASE IF NOT EXISTS `erp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `erp`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME     DEFAULT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL COMMENT '角色名称',
  `code`        VARCHAR(50)  NOT NULL COMMENT '角色编码',
  `remark`      VARCHAR(255) DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME     DEFAULT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id`        BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID，0为根',
  `name`      VARCHAR(50)  NOT NULL COMMENT '菜单名称',
  `path`      VARCHAR(120) DEFAULT NULL COMMENT '路由地址',
  `icon`      VARCHAR(50)  DEFAULT NULL COMMENT 'lucide 图标名',
  `type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0目录 1菜单',
  `perms`     VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `sort`      INT          NOT NULL DEFAULT 0,
  `visible`   TINYINT      NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单';

DROP TABLE IF EXISTS `erp_category`;
CREATE TABLE `erp_category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT      NOT NULL DEFAULT 0,
  `name`        VARCHAR(50) NOT NULL,
  `sort`        INT         NOT NULL DEFAULT 0,
  `create_time` DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `code`           VARCHAR(50)   NOT NULL COMMENT '商品编码',
  `name`           VARCHAR(100)  NOT NULL COMMENT '商品名称',
  `category_id`    BIGINT        DEFAULT NULL,
  `spec`           VARCHAR(100)  DEFAULT NULL COMMENT '规格型号',
  `unit`           VARCHAR(20)   DEFAULT '件',
  `purchase_price` DECIMAL(14,2) NOT NULL DEFAULT 0,
  `sale_price`     DECIMAL(14,2) NOT NULL DEFAULT 0,
  `safety_stock`   INT           NOT NULL DEFAULT 0 COMMENT '安全库存',
  `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '1在售 0停用',
  `remark`         VARCHAR(255)  DEFAULT NULL,
  `create_time`    DATETIME      DEFAULT NULL,
  `update_time`    DATETIME      DEFAULT NULL,
  `deleted`        TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

DROP TABLE IF EXISTS `erp_warehouse`;
CREATE TABLE `erp_warehouse` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(50)  NOT NULL,
  `name`        VARCHAR(100) NOT NULL,
  `address`     VARCHAR(255) DEFAULT NULL,
  `manager`     VARCHAR(50)  DEFAULT NULL,
  `phone`       VARCHAR(20)  DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wh_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库';

DROP TABLE IF EXISTS `erp_inventory`;
CREATE TABLE `erp_inventory` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `warehouse_id` BIGINT   NOT NULL,
  `product_id`   BIGINT   NOT NULL,
  `qty`          INT      NOT NULL DEFAULT 0 COMMENT '库存数量',
  `create_time`  DATETIME DEFAULT NULL,
  `update_time`  DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wh_product` (`warehouse_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存';

DROP TABLE IF EXISTS `erp_stock_record`;
CREATE TABLE `erp_stock_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `type`         VARCHAR(10)  NOT NULL COMMENT 'IN/OUT',
  `warehouse_id` BIGINT       NOT NULL,
  `product_id`   BIGINT       NOT NULL,
  `qty`          INT          NOT NULL,
  `biz_type`     VARCHAR(20)  DEFAULT NULL COMMENT 'PURCHASE/SALES/ADJUST',
  `biz_no`       VARCHAR(50)  DEFAULT NULL COMMENT '关联单号',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `create_by`    VARCHAR(50)  DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出入库流水';

DROP TABLE IF EXISTS `erp_purchase_order`;
CREATE TABLE `erp_purchase_order` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(50)   NOT NULL,
  `supplier`     VARCHAR(100)  NOT NULL,
  `warehouse_id` BIGINT        NOT NULL,
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/RECEIVED/CANCELLED',
  `total_amount` DECIMAL(14,2) NOT NULL DEFAULT 0,
  `remark`       VARCHAR(255)  DEFAULT NULL,
  `create_by`    VARCHAR(50)   DEFAULT NULL,
  `create_time`  DATETIME      DEFAULT NULL,
  `update_time`  DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单';

DROP TABLE IF EXISTS `erp_purchase_order_item`;
CREATE TABLE `erp_purchase_order_item` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT        NOT NULL,
  `product_id` BIGINT        NOT NULL,
  `qty`        INT           NOT NULL,
  `price`      DECIMAL(14,2) NOT NULL DEFAULT 0,
  `amount`     DECIMAL(14,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细';

DROP TABLE IF EXISTS `erp_sales_order`;
CREATE TABLE `erp_sales_order` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(50)   NOT NULL,
  `customer`     VARCHAR(100)  NOT NULL,
  `warehouse_id` BIGINT        NOT NULL,
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/SHIPPED/CANCELLED',
  `total_amount` DECIMAL(14,2) NOT NULL DEFAULT 0,
  `remark`       VARCHAR(255)  DEFAULT NULL,
  `create_by`    VARCHAR(50)   DEFAULT NULL,
  `create_time`  DATETIME      DEFAULT NULL,
  `update_time`  DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_so_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单';

DROP TABLE IF EXISTS `erp_sales_order_item`;
CREATE TABLE `erp_sales_order_item` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT        NOT NULL,
  `product_id` BIGINT        NOT NULL,
  `qty`        INT           NOT NULL,
  `price`      DECIMAL(14,2) NOT NULL DEFAULT 0,
  `amount`     DECIMAL(14,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细';

-- ============================ 种子数据 ============================
INSERT INTO `sys_user` (`id`,`username`,`password`,`nickname`,`email`,`phone`,`status`,`create_time`,`update_time`) VALUES
(1,'admin','$2b$10$juAb6c/LpOTExJeKpuI8Q.zSDokiIRWMQClo2TCtbvtzgLZ7Ug2PW','系统管理员','admin@erp.com','13800000000',1,NOW(),NOW()),
(2,'warehouse','$2b$10$juAb6c/LpOTExJeKpuI8Q.zSDokiIRWMQClo2TCtbvtzgLZ7Ug2PW','仓库管理员','wh@erp.com','13800000001',1,NOW(),NOW());

INSERT INTO `sys_role` (`id`,`name`,`code`,`remark`,`status`,`create_time`,`update_time`) VALUES
(1,'超级管理员','admin','拥有全部权限',1,NOW(),NOW()),
(2,'仓库管理员','warehouse','仅库存相关权限',1,NOW(),NOW());

INSERT INTO `sys_menu` (`id`,`parent_id`,`name`,`path`,`icon`,`type`,`perms`,`sort`,`visible`) VALUES
(1,0,'仪表盘','/dashboard','LayoutDashboard',1,'dashboard:view',1,1),
(2,0,'系统管理','/system',NULL,0,NULL,2,1),
(21,2,'用户管理','/system/users','Users',1,'system:user:list',1,1),
(22,2,'角色管理','/system/roles','ShieldCheck',1,'system:role:list',2,1),
(23,2,'菜单管理','/system/menus','ListTree',1,'system:menu:list',3,1),
(3,0,'商品管理','/product',NULL,0,NULL,3,1),
(31,3,'商品分类','/product/categories','Tags',1,'product:category:list',1,1),
(32,3,'商品档案','/product/list','Package',1,'product:list',2,1),
(4,0,'库存管理','/inventory',NULL,0,NULL,4,1),
(41,4,'库存查询','/inventory/list','Boxes',1,'inventory:list',1,1),
(42,4,'出入库记录','/inventory/records','ArrowLeftRight',1,'inventory:record:list',2,1),
(43,4,'仓库管理','/inventory/warehouses','Warehouse',1,'inventory:warehouse:list',3,1),
(5,0,'采购管理','/purchase',NULL,0,NULL,5,1),
(51,5,'采购订单','/purchase/orders','ShoppingCart',1,'purchase:order:list',1,1),
(6,0,'销售管理','/sales',NULL,0,NULL,6,1),
(61,6,'销售订单','/sales/orders','Truck',1,'sales:order:list',1,1);

INSERT INTO `sys_user_role` (`user_id`,`role_id`) VALUES (1,1),(2,2);

INSERT INTO `sys_role_menu` (`role_id`,`menu_id`)
SELECT 1, id FROM `sys_menu`;
INSERT INTO `sys_role_menu` (`role_id`,`menu_id`) VALUES
(2,1),(2,4),(2,41),(2,42),(2,43);

INSERT INTO `erp_category` (`id`,`parent_id`,`name`,`sort`,`create_time`) VALUES
(1,0,'电子产品',1,NOW()),
(2,0,'办公用品',2,NOW()),
(3,0,'原材料',3,NOW()),
(11,1,'手机数码',1,NOW()),
(12,1,'电脑外设',2,NOW()),
(21,2,'纸张耗材',1,NOW()),
(31,3,'金属材料',1,NOW());

INSERT INTO `erp_product` (`id`,`code`,`name`,`category_id`,`spec`,`unit`,`purchase_price`,`sale_price`,`safety_stock`,`status`,`remark`,`create_time`,`update_time`) VALUES
(1,'P1001','iPhone 15 128G',11,'A3092 黑色','台',4500.00,5999.00,10,1,'样机',NOW(),NOW()),
(2,'P1002','ThinkPad X1 Carbon',12,'14英寸 i7/16G/512G','台',7800.00,9999.00,5,1,NULL,NOW(),NOW()),
(3,'P1003','罗技 MX Master 3S',12,'石墨黑','个',450.00,699.00,20,1,NULL,NOW(),NOW()),
(4,'P1004','戴尔 U2723QE 显示器',12,'27英寸 4K','台',2800.00,3599.00,8,1,NULL,NOW(),NOW()),
(5,'P2001','A4 复印纸',21,'70g 500张/包 5包/箱','箱',120.00,168.00,30,1,NULL,NOW(),NOW()),
(6,'P2002','晨光中性笔',21,'0.5mm 12支/盒','盒',35.00,59.00,50,1,NULL,NOW(),NOW()),
(7,'P3001','冷轧钢板',31,'SPCC 1.0mm','吨',4200.00,5100.00,8,1,NULL,NOW(),NOW()),
(8,'P3002','铝合金型材',31,'6063-T5','吨',9800.00,11200.00,5,1,NULL,NOW(),NOW());

INSERT INTO `erp_warehouse` (`id`,`code`,`name`,`address`,`manager`,`phone`,`status`,`create_time`) VALUES
(1,'WH-SH','上海中心仓','上海市青浦区华新镇物流园 8 号','王建国','13900000001',1,NOW()),
(2,'WH-GZ','广州分仓','广州市白云区太和镇兴丰路 66 号','李海峰','13900000002',1,NOW()),
(3,'WH-BJ','北京备件仓','北京市通州区马驹桥物流基地','赵敏','13900000003',1,NOW());

INSERT INTO `erp_inventory` (`warehouse_id`,`product_id`,`qty`,`create_time`,`update_time`) VALUES
(1,1,25,NOW(),NOW()),
(1,2,3,NOW(),NOW()),
(1,3,48,NOW(),NOW()),
(1,4,6,NOW(),NOW()),
(1,5,120,NOW(),NOW()),
(1,7,4,NOW(),NOW()),
(2,1,30,NOW(),NOW()),
(2,3,60,NOW(),NOW()),
(2,5,80,NOW(),NOW()),
(2,6,25,NOW(),NOW()),
(3,2,10,NOW(),NOW()),
(3,4,15,NOW(),NOW()),
(3,8,2,NOW(),NOW());

INSERT INTO `erp_purchase_order` (`id`,`order_no`,`supplier`,`warehouse_id`,`status`,`total_amount`,`remark`,`create_by`,`create_time`,`update_time`) VALUES
(1,'PO202610200001','上海华讯电子有限公司',1,'RECEIVED',54000.00,'季度补货','admin','2026-10-20 09:30:00','2026-10-21 10:00:00'),
(2,'PO202610210002','江苏恒通办公用品有限公司',2,'APPROVED',12000.00,NULL,'admin','2026-10-21 14:20:00','2026-10-22 09:10:00'),
(3,'PO202610220003','广东精工材料有限公司',1,'PENDING',21000.00,'钢材补库','admin','2026-10-22 16:05:00','2026-10-22 16:05:00');

INSERT INTO `erp_purchase_order_item` (`order_id`,`product_id`,`qty`,`price`,`amount`) VALUES
(1,1,10,4500.00,45000.00),
(1,3,20,450.00,9000.00),
(2,5,100,120.00,12000.00),
(3,7,5,4200.00,21000.00);

INSERT INTO `erp_sales_order` (`id`,`order_no`,`customer`,`warehouse_id`,`status`,`total_amount`,`remark`,`create_by`,`create_time`,`update_time`) VALUES
(1,'SO202610190001','杭州云启科技有限公司',1,'SHIPPED',15493.00,NULL,'admin','2026-10-19 10:15:00','2026-10-20 11:00:00'),
(2,'SO202610210002','南京博远商贸有限公司',2,'APPROVED',3950.00,'待发货','admin','2026-10-21 11:40:00','2026-10-22 09:30:00'),
(3,'SO202610220003','苏州智造装备有限公司',1,'PENDING',9999.00,NULL,'admin','2026-10-22 15:20:00','2026-10-22 15:20:00');

INSERT INTO `erp_sales_order_item` (`order_id`,`product_id`,`qty`,`price`,`amount`) VALUES
(1,1,2,5999.00,11998.00),
(1,3,5,699.00,3495.00),
(2,5,20,168.00,3360.00),
(2,6,10,59.00,590.00),
(3,2,1,9999.00,9999.00);

INSERT INTO `erp_stock_record` (`type`,`warehouse_id`,`product_id`,`qty`,`biz_type`,`biz_no`,`remark`,`create_by`,`create_time`) VALUES
('IN',1,1,10,'PURCHASE','PO202610200001','采购入库','admin','2026-10-21 10:00:00'),
('IN',1,3,20,'PURCHASE','PO202610200001','采购入库','admin','2026-10-21 10:00:00'),
('OUT',1,1,2,'SALES','SO202610190001','销售出库','admin','2026-10-20 11:00:00'),
('OUT',1,3,5,'SALES','SO202610190001','销售出库','admin','2026-10-20 11:00:00');

SET FOREIGN_KEY_CHECKS = 1;
