import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '仪表盘' },
        },
        {
          path: 'system/users',
          name: 'users',
          component: () => import('@/views/system/UsersView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'system/roles',
          name: 'roles',
          component: () => import('@/views/system/RolesView.vue'),
          meta: { title: '角色管理' },
        },
        {
          path: 'system/menus',
          name: 'menus',
          component: () => import('@/views/system/MenusView.vue'),
          meta: { title: '菜单管理' },
        },
        {
          path: 'product/categories',
          name: 'categories',
          component: () => import('@/views/product/CategoriesView.vue'),
          meta: { title: '商品分类' },
        },
        {
          path: 'product/list',
          name: 'products',
          component: () => import('@/views/product/ProductsView.vue'),
          meta: { title: '商品档案' },
        },
        {
          path: 'inventory/list',
          name: 'inventory',
          component: () => import('@/views/inventory/InventoryView.vue'),
          meta: { title: '库存查询' },
        },
        {
          path: 'inventory/records',
          name: 'records',
          component: () => import('@/views/inventory/RecordsView.vue'),
          meta: { title: '出入库记录' },
        },
        {
          path: 'inventory/warehouses',
          name: 'warehouses',
          component: () => import('@/views/inventory/WarehousesView.vue'),
          meta: { title: '仓库管理' },
        },
        {
          path: 'purchase/orders',
          name: 'purchase-orders',
          component: () => import('@/views/purchase/PurchaseOrdersView.vue'),
          meta: { title: '采购订单' },
        },
        {
          path: 'sales/orders',
          name: 'sales-orders',
          component: () => import('@/views/sales/SalesOrdersView.vue'),
          meta: { title: '销售订单' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    return auth.token ? { path: '/dashboard' } : true
  }
  if (!auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return { path: '/login' }
    }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 云帆 ERP` : '云帆 ERP'
})

export default router
