<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  AlertTriangle,
  ArrowLeftRight,
  ArrowUpRight,
  Boxes,
  CircleDollarSign,
  ClipboardList,
  Package,
  ShoppingCart,
  Truck,
  Warehouse,
} from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Card from '@/components/ui/card/Card.vue'
import CardContent from '@/components/ui/card/CardContent.vue'
import CardHeader from '@/components/ui/card/CardHeader.vue'
import CardTitle from '@/components/ui/card/CardTitle.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Table from '@/components/ui/table/Table.vue'
import TableHeader from '@/components/ui/table/TableHeader.vue'
import TableBody from '@/components/ui/table/TableBody.vue'
import TableRow from '@/components/ui/table/TableRow.vue'
import TableHead from '@/components/ui/table/TableHead.vue'
import TableCell from '@/components/ui/table/TableCell.vue'
import { api } from '@/lib/http'
import type { Inventory, StockRecord } from '@/lib/types'
import { money, number } from '@/lib/format'

interface Stats {
  productCount: number
  warehouseCount: number
  totalQty: number
  totalValue: number
  purchasePending: number
  salesPending: number
  lowStock: Inventory[]
  lowStockCount: number
  recentRecords: StockRecord[]
}

const stats = ref<Stats>({
  productCount: 0,
  warehouseCount: 0,
  totalQty: 0,
  totalValue: 0,
  purchasePending: 0,
  salesPending: 0,
  lowStock: [],
  lowStockCount: 0,
  recentRecords: [],
})
const loading = ref(true)

const cards = [
  { key: 'productCount', label: '在售商品', icon: Package, tone: 'bg-blue-50 text-blue-600', suffix: '种' },
  { key: 'totalQty', label: '库存总量', icon: Boxes, tone: 'bg-indigo-50 text-indigo-600', suffix: '件' },
  { key: 'totalValue', label: '库存货值', icon: CircleDollarSign, tone: 'bg-emerald-50 text-emerald-600', suffix: '元' },
  { key: 'pending', label: '待处理单据', icon: ClipboardList, tone: 'bg-amber-50 text-amber-600', suffix: '单' },
] as const

function cardValue(key: (typeof cards)[number]['key']) {
  if (key === 'pending') return number(stats.value.purchasePending + stats.value.salesPending)
  if (key === 'totalValue') return money(stats.value.totalValue)
  return number(stats.value[key])
}

async function load() {
  loading.value = true
  try {
    stats.value = await api.get<Stats>('/dashboard/stats')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="仪表盘" description="掌握商品、库存与业务单据的整体运行情况">
      <template #actions>
        <RouterLink
          to="/purchase/orders"
          class="inline-flex h-9 items-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground shadow-sm transition hover:bg-primary/90"
        >
          <ShoppingCart class="h-4 w-4" />新建采购
        </RouterLink>
      </template>
    </PageHeader>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card v-for="card in cards" :key="card.key">
        <CardContent class="p-5">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-xs font-medium text-slate-500">{{ card.label }}</p>
              <p class="mt-2 text-2xl font-semibold tracking-tight text-slate-900">
                {{ cardValue(card.key) }}
              </p>
              <p class="mt-1 text-[11px] text-slate-400">
                <template v-if="card.key === 'pending'">
                  采购 {{ stats.purchasePending }} · 销售 {{ stats.salesPending }}
                </template>
                <template v-else-if="card.key === 'productCount'">
                  覆盖 {{ stats.warehouseCount }} 个仓库
                </template>
                <template v-else-if="card.key === 'totalValue'">
                  按采购成本估算
                </template>
                <template v-else>
                  当前所有仓库合计
                </template>
              </p>
            </div>
            <div :class="['flex h-10 w-10 items-center justify-center rounded-xl', card.tone]">
              <component :is="card.icon" class="h-5 w-5" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="mt-4 grid gap-4 xl:grid-cols-3">
      <Card class="xl:col-span-2">
        <CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
          <div class="flex items-center gap-2">
            <CardTitle>库存预警</CardTitle>
            <Badge v-if="stats.lowStockCount > 0" variant="warning">{{ stats.lowStockCount }} 项</Badge>
          </div>
          <RouterLink to="/inventory/list" class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline">
            查看库存<ArrowUpRight class="h-3.5 w-3.5" />
          </RouterLink>
        </CardHeader>
        <CardContent class="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead class="pl-5">商品</TableHead>
                <TableHead>仓库</TableHead>
                <TableHead class="text-right">当前库存</TableHead>
                <TableHead class="text-right">安全库存</TableHead>
                <TableHead class="pr-5">状态</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="item in stats.lowStock" :key="item.id">
                <TableCell class="pl-5">
                  <p class="font-medium text-slate-800">{{ item.productName }}</p>
                  <p class="text-[11px] text-slate-400">{{ item.productCode }}</p>
                </TableCell>
                <TableCell class="text-slate-600">{{ item.warehouseName }}</TableCell>
                <TableCell class="text-right font-medium text-red-600">
                  {{ item.qty }} {{ item.unit }}
                </TableCell>
                <TableCell class="text-right text-slate-500">{{ item.safetyStock }}</TableCell>
                <TableCell class="pr-5">
                  <Badge variant="destructive">
                    <AlertTriangle class="mr-1 h-3 w-3" />库存不足
                  </Badge>
                </TableCell>
              </TableRow>
              <TableRow v-if="!loading && stats.lowStock.length === 0">
                <TableCell colspan="5" class="py-10 text-center text-sm text-muted-foreground">
                  库存充足，暂无预警
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="pb-3">
          <CardTitle>快捷入口</CardTitle>
        </CardHeader>
        <CardContent class="grid gap-2.5">
          <RouterLink
            v-for="link in [
              { label: '商品档案', desc: '维护商品与价格', to: '/product/list', icon: Package },
              { label: '库存查询', desc: '查看各仓实时库存', to: '/inventory/list', icon: Warehouse },
              { label: '出入库流水', desc: '追溯库存变动', to: '/inventory/records', icon: ArrowLeftRight },
              { label: '采购订单', desc: '采购、审核与入库', to: '/purchase/orders', icon: ShoppingCart },
              { label: '销售订单', desc: '销售、审核与出库', to: '/sales/orders', icon: Truck },
            ]"
            :key="link.to"
            :to="link.to"
            class="group flex items-center gap-3 rounded-lg border border-slate-100 px-3.5 py-3 transition hover:border-blue-200 hover:bg-blue-50/40"
          >
            <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-50 text-slate-500 group-hover:bg-white group-hover:text-blue-600">
              <component :is="link.icon" class="h-4 w-4" />
            </div>
            <div class="min-w-0">
              <p class="text-sm font-medium text-slate-800">{{ link.label }}</p>
              <p class="text-[11px] text-slate-400">{{ link.desc }}</p>
            </div>
            <ArrowUpRight class="ml-auto h-4 w-4 text-slate-300 group-hover:text-blue-500" />
          </RouterLink>
        </CardContent>
      </Card>
    </div>

    <Card class="mt-4">
      <CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
        <CardTitle>最近库存流水</CardTitle>
        <RouterLink to="/inventory/records" class="text-xs font-medium text-primary hover:underline">全部记录</RouterLink>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="pl-5">时间</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>商品</TableHead>
              <TableHead>仓库</TableHead>
              <TableHead class="text-right">数量</TableHead>
              <TableHead class="pr-5">业务单号</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="record in stats.recentRecords" :key="record.id">
              <TableCell class="pl-5 text-xs text-slate-500">{{ record.createTime }}</TableCell>
              <TableCell>
                <Badge :variant="record.type === 'IN' ? 'success' : 'warning'">
                  {{ record.type === 'IN' ? '入库' : '出库' }}
                </Badge>
              </TableCell>
              <TableCell>
                <p class="font-medium text-slate-800">{{ record.productName }}</p>
                <p class="text-[11px] text-slate-400">{{ record.productCode }}</p>
              </TableCell>
              <TableCell class="text-slate-600">{{ record.warehouseName }}</TableCell>
              <TableCell class="text-right font-medium" :class="record.type === 'IN' ? 'text-emerald-600' : 'text-amber-600'">
                {{ record.type === 'IN' ? '+' : '-' }}{{ record.qty }}
              </TableCell>
              <TableCell class="pr-5 text-xs text-slate-500">{{ record.bizNo || '-' }}</TableCell>
            </TableRow>
            <TableRow v-if="!loading && stats.recentRecords.length === 0">
              <TableCell colspan="6" class="py-10 text-center text-sm text-muted-foreground">暂无流水记录</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
