<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Check, Eye, Plus, Search, Trash2, Truck, Undo2 } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Label from '@/components/ui/label/Label.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import Table from '@/components/ui/table/Table.vue'
import TableHeader from '@/components/ui/table/TableHeader.vue'
import TableBody from '@/components/ui/table/TableBody.vue'
import TableRow from '@/components/ui/table/TableRow.vue'
import TableHead from '@/components/ui/table/TableHead.vue'
import TableCell from '@/components/ui/table/TableCell.vue'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import { api } from '@/lib/http'
import type { PageResult, Product, SalesOrder, Warehouse } from '@/lib/types'
import { money } from '@/lib/format'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

interface FormItem {
  productId: string
  qty: string
  price: string
}

const orders = ref<SalesOrder[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref('ALL')
const keyword = ref('')
const loading = ref(false)

const warehouses = ref<Warehouse[]>([])
const products = ref<Product[]>([])

const createOpen = ref(false)
const saving = ref(false)
const form = reactive({
  customer: '',
  warehouseId: '',
  remark: '',
  items: [] as FormItem[],
})

const detailOpen = ref(false)
const detail = ref<SalesOrder | null>(null)

const formTotal = computed(() =>
  form.items.reduce((sum, item) => sum + Number(item.qty || 0) * Number(item.price || 0), 0),
)

function itemAmount(item: FormItem) {
  return Number(item.qty || 0) * Number(item.price || 0)
}

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<SalesOrder>>('/sales-orders', {
      page: page.value,
      size,
      status: statusFilter.value === 'ALL' ? undefined : statusFilter.value,
      keyword: keyword.value || undefined,
    })
    orders.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  warehouses.value = await api.get<Warehouse[]>('/warehouses/all')
  products.value = await api.get<Product[]>('/products/all')
}

function search() {
  page.value = 1
  load()
}

function addItem() {
  form.items.push({ productId: '', qty: '1', price: '' })
}

function removeItem(index: number) {
  form.items.splice(index, 1)
}

function onProductChange(item: FormItem) {
  const product = products.value.find((p) => p.id === Number(item.productId))
  if (product) {
    item.price = String(product.salePrice ?? '')
  }
}

function openCreate() {
  Object.assign(form, {
    customer: '',
    warehouseId: warehouses.value[0] ? String(warehouses.value[0].id) : '',
    remark: '',
    items: [{ productId: '', qty: '1', price: '' }],
  })
  createOpen.value = true
}

async function submit() {
  if (!form.customer.trim()) return toast.error('请填写客户名称')
  if (!form.warehouseId) return toast.error('请选择出库仓库')
  if (form.items.length === 0) return toast.error('请至少添加一条明细')
  for (const item of form.items) {
    if (!item.productId) return toast.error('请为每条明细选择商品')
    if (Number(item.qty) <= 0) return toast.error('明细数量必须大于 0')
    if (item.price === '' || Number(item.price) < 0) return toast.error('请填写正确的销售单价')
  }
  saving.value = true
  try {
    await api.post('/sales-orders', {
      customer: form.customer,
      warehouseId: Number(form.warehouseId),
      remark: form.remark,
      items: form.items.map((item) => ({
        productId: Number(item.productId),
        qty: Number(item.qty),
        price: Number(item.price),
      })),
    })
    toast.success('销售订单已创建，等待审核')
    createOpen.value = false
    page.value = 1
    load()
  } finally {
    saving.value = false
  }
}

async function showDetail(order: SalesOrder) {
  detail.value = await api.get<SalesOrder>(`/sales-orders/${order.id}`)
  detailOpen.value = true
}

async function approve(order: SalesOrder) {
  await api.post(`/sales-orders/${order.id}/approve`)
  toast.success('销售订单已审核')
  load()
}

async function ship(order: SalesOrder) {
  const ok = await confirmAction(
    '确认销售出库',
    `订单「${order.orderNo}」出库后，将按明细扣减仓库库存并生成出库流水。`,
    '确认出库',
  )
  if (!ok) return
  await api.post(`/sales-orders/${order.id}/ship`)
  toast.success('销售出库完成')
  load()
}

async function cancel(order: SalesOrder) {
  const ok = await confirmAction('取消订单', `确定取消销售订单「${order.orderNo}」吗？`, '确认取消')
  if (!ok) return
  await api.post(`/sales-orders/${order.id}/cancel`)
  toast.success('销售订单已取消')
  load()
}

watch(statusFilter, () => {
  page.value = 1
  load()
})

onMounted(() => {
  load()
  loadOptions()
})
</script>

<template>
  <div>
    <PageHeader title="销售订单" description="销售下单、审核、出库全流程管理，出库自动扣减库存">
      <template #actions>
        <Button @click="openCreate"><Plus class="h-4 w-4" />新建销售单</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 lg:flex-row lg:items-center">
        <div class="relative w-full lg:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索单号或客户" @keyup.enter="search" />
        </div>
        <Select v-model="statusFilter">
          <SelectTrigger class="w-full lg:w-36"><SelectValue placeholder="全部状态" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部状态</SelectItem>
            <SelectItem value="PENDING">待审核</SelectItem>
            <SelectItem value="APPROVED">已审核</SelectItem>
            <SelectItem value="SHIPPED">已出库</SelectItem>
            <SelectItem value="CANCELLED">已取消</SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 张销售单</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">销售单号</TableHead>
            <TableHead>客户</TableHead>
            <TableHead>出库仓库</TableHead>
            <TableHead class="text-right">销售金额</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>创建人</TableHead>
            <TableHead>创建时间</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="order in orders" :key="order.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
                  <Truck class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ order.orderNo }}</p>
                  <p class="text-[11px] text-slate-400">销售订单</p>
                </div>
              </div>
            </TableCell>
            <TableCell class="text-sm text-slate-700">{{ order.customer }}</TableCell>
            <TableCell class="text-sm text-slate-600">{{ order.warehouseName }}</TableCell>
            <TableCell class="text-right text-sm font-semibold text-slate-800">{{ money(order.totalAmount) }}</TableCell>
            <TableCell><OrderStatusBadge :status="order.status" /></TableCell>
            <TableCell class="text-sm text-slate-600">{{ order.createBy || '-' }}</TableCell>
            <TableCell class="text-xs text-slate-500">{{ order.createTime }}</TableCell>
            <TableCell class="pr-5">
              <div class="flex items-center justify-end gap-1">
                <Button variant="ghost" size="sm" @click="showDetail(order)"><Eye class="h-3.5 w-3.5" />详情</Button>
                <Button v-if="order.status === 'PENDING'" variant="ghost" size="sm" class="text-blue-600 hover:bg-blue-50 hover:text-blue-600" @click="approve(order)">
                  <Check class="h-3.5 w-3.5" />审核
                </Button>
                <Button v-if="order.status === 'APPROVED'" variant="ghost" size="sm" class="text-emerald-600 hover:bg-emerald-50 hover:text-emerald-600" @click="ship(order)">
                  <Truck class="h-3.5 w-3.5" />出库
                </Button>
                <Button
                  v-if="order.status === 'PENDING' || order.status === 'APPROVED'"
                  variant="ghost"
                  size="sm"
                  class="text-red-600 hover:bg-red-50 hover:text-red-600"
                  @click="cancel(order)"
                >
                  <Undo2 class="h-3.5 w-3.5" />取消
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && orders.length === 0">
            <TableCell colspan="8" class="py-14 text-center text-sm text-muted-foreground">暂无销售订单</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="createOpen">
      <DialogContent class="max-w-3xl">
        <DialogHeader>
          <DialogTitle>新建销售订单</DialogTitle>
          <DialogDescription>提交后状态为「待审核」，审核通过后可执行出库</DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>客户名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.customer" placeholder="如 杭州云启科技有限公司" />
          </div>
          <div class="space-y-1.5">
            <Label>出库仓库 <span class="text-red-500">*</span></Label>
            <Select v-model="form.warehouseId">
              <SelectTrigger><SelectValue placeholder="请选择仓库" /></SelectTrigger>
              <SelectContent>
                <SelectItem v-for="warehouse in warehouses" :key="warehouse.id" :value="String(warehouse.id)">
                  {{ warehouse.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <Label>销售明细 <span class="text-red-500">*</span></Label>
            <Button variant="outline" size="sm" @click="addItem"><Plus class="h-3.5 w-3.5" />添加明细</Button>
          </div>
          <div class="max-h-72 space-y-2 overflow-y-auto rounded-lg border border-slate-200 p-3 scrollbar-thin">
            <div v-for="(item, index) in form.items" :key="index" class="rounded-lg bg-slate-50 p-2.5">
              <div class="grid grid-cols-12 gap-2">
                <div class="col-span-12 sm:col-span-5">
                  <Select v-model="item.productId" @update:model-value="() => onProductChange(item)">
                    <SelectTrigger class="bg-white"><SelectValue placeholder="选择商品" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem v-for="product in products" :key="product.id" :value="String(product.id)">
                        {{ product.name }}（{{ product.code }}）
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div class="col-span-4 sm:col-span-2">
                  <Input v-model="item.qty" type="number" min="1" placeholder="数量" />
                </div>
                <div class="col-span-4 sm:col-span-2">
                  <Input v-model="item.price" type="number" min="0" step="0.01" placeholder="单价" />
                </div>
                <div class="col-span-3 flex items-center justify-end text-sm font-medium text-slate-700 sm:col-span-2">
                  {{ money(itemAmount(item)) }}
                </div>
                <div class="col-span-1 flex items-center justify-end">
                  <Button variant="ghost" size="icon" class="h-9 w-9 text-red-500 hover:bg-red-50 hover:text-red-600" @click="removeItem(index)">
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>
            <p v-if="form.items.length === 0" class="py-6 text-center text-sm text-slate-400">
              暂无明细，点击右上角「添加明细」
            </p>
          </div>
        </div>

        <div class="space-y-1.5">
          <Label>备注</Label>
          <Textarea v-model="form.remark" placeholder="选填，如交付要求、物流信息" />
        </div>

        <div class="flex items-center justify-end gap-2 rounded-lg bg-slate-50 px-4 py-3">
          <span class="text-xs text-slate-500">销售合计</span>
          <span class="text-xl font-semibold text-slate-900">{{ money(formTotal) }}</span>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="createOpen = false">取消</Button>
          <Button :disabled="saving" @click="submit">{{ saving ? '提交中...' : '提交订单' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-model:open="detailOpen">
      <DialogContent class="max-w-2xl">
        <DialogHeader>
          <div class="flex items-center gap-2">
            <DialogTitle>销售订单详情</DialogTitle>
            <OrderStatusBadge v-if="detail" :status="detail.status" />
          </div>
          <DialogDescription>{{ detail?.orderNo }}</DialogDescription>
        </DialogHeader>

        <div v-if="detail" class="grid gap-3 rounded-lg bg-slate-50 p-4 text-sm sm:grid-cols-2">
          <div><span class="text-slate-400">客户：</span>{{ detail.customer }}</div>
          <div><span class="text-slate-400">出库仓库：</span>{{ detail.warehouseName }}</div>
          <div><span class="text-slate-400">创建人：</span>{{ detail.createBy }}</div>
          <div><span class="text-slate-400">创建时间：</span>{{ detail.createTime }}</div>
          <div class="sm:col-span-2"><span class="text-slate-400">备注：</span>{{ detail.remark || '-' }}</div>
        </div>

        <div v-if="detail" class="overflow-hidden rounded-lg border border-slate-200">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>商品</TableHead>
                <TableHead class="text-right">数量</TableHead>
                <TableHead class="text-right">单价</TableHead>
                <TableHead class="text-right">小计</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="item in detail.items" :key="item.id">
                <TableCell>
                  <p class="font-medium text-slate-800">{{ item.productName }}</p>
                  <p class="text-[11px] text-slate-400">{{ item.productCode }}</p>
                </TableCell>
                <TableCell class="text-right text-sm text-slate-600">{{ item.qty }} {{ item.unit }}</TableCell>
                <TableCell class="text-right text-sm text-slate-600">{{ money(item.price) }}</TableCell>
                <TableCell class="text-right text-sm font-medium text-slate-800">{{ money(item.amount) }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <div class="flex items-center justify-between">
          <div class="text-xs text-slate-500">共 {{ detail?.items?.length || 0 }} 项明细</div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-slate-500">合计</span>
            <span class="text-lg font-semibold text-slate-900">{{ money(detail?.totalAmount) }}</span>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="detailOpen = false">关闭</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
