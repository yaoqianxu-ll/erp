# 云帆 ERP 进销存系统

一套可直接运行的前后端分离 ERP 进销存演示系统，覆盖 **系统管理、商品档案、库存管理、采购管理、销售管理** 五大模块，包含从采购入库到销售出库的完整业务闭环。

- 后端：Spring Boot 3.3 + Java 17 + MyBatis-Plus + Spring Security + JWT
- 前端：Vue 3 + TypeScript + Vite + Tailwind CSS + shadcn-vue 风格组件
- 数据库：MySQL 8.0

---

## 一、功能模块

| 模块 | 功能 |
| --- | --- |
| 仪表盘 | 商品数、库存总量、库存货值、待处理单据、库存预警、最近出入库流水 |
| 系统管理 | 用户管理、角色管理（菜单树授权）、菜单管理 |
| 商品管理 | 商品分类、商品档案（编码、规格、单位、采购价、销售价、安全库存） |
| 库存管理 | 实时库存查询、库存调整、出入库流水、仓库管理、低库存预警 |
| 采购管理 | 采购订单创建、审核、入库、取消；入库自动增加库存并记录流水 |
| 销售管理 | 销售订单创建、审核、出库、取消；出库自动校验并扣减库存 |

### 业务流程

```
采购：待审核 PENDING ──审核──> 已审核 APPROVED ──入库──> 已入库 RECEIVED
销售：待审核 PENDING ──审核──> 已审核 APPROVED ──出库──> 已出库 SHIPPED
任意未完成单据可取消为 CANCELLED
```

- 采购入库：按明细增加对应仓库库存，写入 `erp_stock_record` 入库流水。
- 销售出库：使用 `UPDATE ... WHERE qty >= ?` 条件扣减，库存不足时整单回滚。
- 订单状态流转使用条件更新，避免重复审核 / 重复出入库。

---

## 二、目录结构

```
erp/
├── erp-server/                 # 后端 Spring Boot 工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/erp/
│       │   ├── auth/           # 登录、当前用户
│       │   ├── common/         # 统一返回、异常处理
│       │   ├── config/         # Security / MyBatis-Plus 配置
│       │   ├── dashboard/      # 仪表盘统计
│       │   ├── inventory/      # 仓库、库存、流水
│       │   ├── product/        # 分类、商品
│       │   ├── security/       # JWT 工具与过滤器
│       │   ├── system/         # 用户、角色、菜单
│       │   └── trade/          # 采购单、销售单
│       └── resources/application.yml
├── erp-web/                    # 前端 Vue 3 工程
│   ├── src/
│   │   ├── components/ui/      # shadcn-vue 风格基础组件
│   │   ├── layouts/            # 侧边栏 + 顶栏布局
│   │   ├── views/              # 各业务页面
│   │   ├── router/             # 路由与菜单权限守卫
│   │   ├── stores/             # Pinia 状态（登录用户 / 菜单）
│   │   └── lib/                # axios 封装、工具函数、toast
│   └── vite.config.ts
├── sql/erp.sql                 # 建库建表 + 演示种子数据
└── README.md
```

---

## 三、快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0

### 2. 初始化数据库

```bash
mysql -uroot -p < sql/erp.sql
```

脚本会创建 `erp` 库、14 张表，并写入演示数据（用户、角色、菜单、商品、仓库、库存、采购/销售订单）。

如需修改数据库连接，编辑 `erp-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/erp?...
    username: root
    password: 你的密码
```

### 3. 启动后端（端口 8080）

```bash
cd erp-server
mvn spring-boot:run
# 或打包运行
mvn -DskipTests package
java -jar target/erp-server-1.0.0.jar
```

### 4. 启动前端（端口 5173）

```bash
cd erp-web
npm install
npm run dev
```

浏览器访问 <http://localhost:5173>。Vite 已将 `/api` 代理到 `http://127.0.0.1:8080`。

生产构建：

```bash
npm run build      # 产物输出到 erp-web/dist
```

---

## 四、演示账号

| 账号 | 密码 | 角色 | 可见模块 |
| --- | --- | --- | --- |
| `admin` | `123456` | 系统管理员 | 全部模块 |
| `warehouse` | `123456` | 仓库管理员 | 仪表盘、库存管理 |

登录页提供演示账号一键填充。

### 权限说明

- 登录使用 Spring Security + JWT，密码使用 BCrypt 存储。
- 后端接口通过 `@PreAuthorize` 做方法级校验，无权限返回 `403`。
- 前端侧边栏由后端返回的菜单动态生成；直接访问无权限路由会被守卫重定向到仪表盘。
- 系统管理、采购/销售、商品与仓库写操作仅 `admin` 可执行；仓库管理员只读库存相关数据。

---

## 五、接口约定

统一返回结构：

```json
{ "code": 200, "message": "success", "data": {} }
```

- `code`：200 成功，400 参数/业务错误，401 未登录，403 无权限，404 不存在，500 服务器错误。
- 除 `/api/auth/login` 外，请求需携带 `Authorization: Bearer <token>`。
- 分页参数：`page`（从 1 开始）、`size`。

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/auth/me` | 当前用户与菜单 |
| GET/POST/PUT/DELETE | `/api/users` | 用户管理 |
| GET/POST/PUT/DELETE | `/api/roles` | 角色与菜单授权 |
| GET/POST/PUT/DELETE | `/api/menus` | 菜单管理 |
| GET/POST/PUT/DELETE | `/api/categories` | 商品分类 |
| GET/POST/PUT/DELETE | `/api/products` | 商品档案 |
| GET/POST/PUT/DELETE | `/api/warehouses` | 仓库管理 |
| GET | `/api/inventory` | 库存分页查询 |
| GET | `/api/inventory/records` | 出入库流水 |
| GET | `/api/inventory/low-stock` | 低库存列表 |
| POST | `/api/inventory/adjust` | 库存调整 |
| GET/POST | `/api/purchase-orders` | 采购单列表 / 新建 |
| POST | `/api/purchase-orders/{id}/approve` | 采购审核 |
| POST | `/api/purchase-orders/{id}/receive` | 采购入库 |
| POST | `/api/purchase-orders/{id}/cancel` | 采购取消 |
| GET/POST | `/api/sales-orders` | 销售单列表 / 新建 |
| POST | `/api/sales-orders/{id}/approve` | 销售审核 |
| POST | `/api/sales-orders/{id}/ship` | 销售出库 |
| POST | `/api/sales-orders/{id}/cancel` | 销售取消 |
| GET | `/api/dashboard/stats` | 仪表盘统计 |

---

## 六、技术要点

- **JWT 鉴权**：`JwtAuthenticationFilter` 解析 token 并注入 `ROLE_<roleKey>`，配合 `@EnableMethodSecurity` 使用方法级注解。
- **防超卖**：销售出库使用带库存条件的原子更新，失败即抛业务异常并回滚事务。
- **幂等状态流转**：审核/入库/出库均使用 `WHERE status = ?` 条件更新，重复操作直接拒绝。
- **统一异常**：业务异常、参数校验、403/404/500 均由 `GlobalExceptionHandler` 转为统一 JSON。
- **动态菜单**：菜单表驱动前端侧边栏与路由权限，角色通过 `sys_role_menu` 关联授权。
- **前端组件**：`components/ui` 内为 shadcn-vue 风格源码组件（Button、Input、Dialog、Select、Table、Card、Badge 等），基于 radix-vue + CVA + tailwind-merge 实现。

---

## 七、常见问题

1. **启动报数据库连接失败**：确认 MySQL 已启动，`application.yml` 中账号密码正确，且已执行 `sql/erp.sql`。
2. **前端接口 404/跨域**：确认后端运行在 8080；开发环境请使用 `npm run dev` 以启用 Vite 代理。
3. **登录后提示无权限**：清除浏览器 localStorage 中的旧 token 后重新登录。
4. **重置演示数据**：重新执行 `mysql -uroot -p < sql/erp.sql` 即可恢复初始状态。

---

> 本项目为教学 / 演示用途，JWT 密钥与数据库密码请在生产环境中替换为安全配置。
