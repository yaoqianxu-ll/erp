export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  status: number
  createTime?: string
  roleIds?: number[]
}

export interface Menu {
  id: number
  parentId: number
  name: string
  path?: string
  icon?: string
  type: number
  perms?: string
  sort: number
  visible: number
  children: Menu[]
}

export interface Role {
  id: number
  name: string
  code: string
  remark?: string
  status: number
  createTime?: string
  menuIds?: number[]
}

export interface Category {
  id: number
  parentId: number
  name: string
  sort: number
  children: Category[]
}

export interface Product {
  id: number
  code: string
  name: string
  categoryId?: number
  categoryName?: string
  spec?: string
  unit?: string
  purchasePrice: number
  salePrice: number
  safetyStock: number
  status: number
  remark?: string
  createTime?: string
}

export interface Warehouse {
  id: number
  code: string
  name: string
  address?: string
  manager?: string
  phone?: string
  status: number
  createTime?: string
}

export interface Inventory {
  id: number
  warehouseId: number
  productId: number
  qty: number
  productName?: string
  productCode?: string
  unit?: string
  warehouseName?: string
  safetyStock?: number
  updateTime?: string
}

export interface StockRecord {
  id: number
  type: 'IN' | 'OUT'
  warehouseId: number
  productId: number
  qty: number
  bizType?: string
  bizNo?: string
  remark?: string
  createBy?: string
  createTime?: string
  productName?: string
  productCode?: string
  unit?: string
  warehouseName?: string
}

export interface OrderItem {
  id?: number
  productId: number
  productName?: string
  productCode?: string
  unit?: string
  qty: number
  price: number
  amount?: number
}

export type OrderStatus = 'PENDING' | 'APPROVED' | 'RECEIVED' | 'SHIPPED' | 'CANCELLED'

export interface PurchaseOrder {
  id: number
  orderNo: string
  supplier: string
  warehouseId: number
  warehouseName?: string
  status: OrderStatus
  totalAmount: number
  remark?: string
  createBy?: string
  createTime?: string
  items: OrderItem[]
}

export interface SalesOrder {
  id: number
  orderNo: string
  customer: string
  warehouseId: number
  warehouseName?: string
  status: OrderStatus
  totalAmount: number
  remark?: string
  createBy?: string
  createTime?: string
  items: OrderItem[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface LoginResult {
  token: string
  user: User
  roles: string[]
  menus: Menu[]
}

export interface MeResult {
  user: User
  roles: string[]
  menus: Menu[]
}
