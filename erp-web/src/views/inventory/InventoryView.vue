<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { AlertTriangle, ArrowLeftRight, Boxes, Search } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Label from '@/components/ui/label/Label.vue'
import Badge from '@/components/ui/badge/Badge.vue'
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
import Textarea from '@/components/ui/textarea/Textarea.vue'
import { api } from '@/lib/http'
import type { Inventory, PageResult, Product, Warehouse } from '@/lib/types'
import { toast } from '@/lib/toast'

const rows = ref<Inventory[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const warehouseFilter = ref('ALL')
const keyword = ref('')
const loading = ref(false)

const warehouses = ref<Warehouse[]>([])
const products = ref<Product[]>([])

const dialogOpen = ref(false)
const saving = ref(false)
const form = reactive({
  warehouseId: '',
  productId: '',
  type: 'IN',
  qty: '1',
  remark: '',
})

const warningCount = computed(() => rows.value.filter((row) => (row.safetyStock ?? 0) >= row.qty).length)

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<Inventory>>('/inventory', {
      page: page.value,
      size,
      warehouseId: warehouseFilter.value === 'ALL' ? undefined : Number(warehouseFilter.value),
      keyword: keyword.value || undefined,
    })
    rows.value = result.records
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

function openAdjust(row?: Inventory) {
  Object.assign(form, {
    warehouseId: row ? String(row.warehouseId) : warehouses.value[0] ? String(warehouses.value[0].id) : '',
    productId: row ? String(row.productId) : '',
    type: 'IN',
    qty: '1',
    remark: '',
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.warehouseId) return toast.error('请选择仓库')
  if (!form.productId) return toast.error('请选择商品')
  if (!form.qty || Number(form.qty) <= 0) return toast.error('请输入正确的数量')
  saving.value = true
  try {
    await api.post('/inventory/adjust', {
      warehouseId: Number(form.warehouseId),
      productId: Number(form.productId),
      type: form.type,
      qty: Number(form.qty),
      remark: form.remark,
    })
    toast.success(form.type === 'IN' ? '库存已增加' : '库存已扣减')
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

watch(warehouseFilter, () => {
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
    <PageHeader title="库存查询" description="按仓库查看实时库存，支持手工调整并自动记录流水">
      <template #actions>
        <Button @click="openAdjust()"><ArrowLeftRight class="h-4 w-4" />库存调整</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 lg:flex-row lg:items-center">
        <div class="relative w-full lg:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索商品名称或编码" @keyup.enter="search" />
        </div>
        <Select v-model="warehouseFilter">
          <SelectTrigger class="w-full lg:w-44"><SelectValue placeholder="全部仓库" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部仓库</SelectItem>
            <SelectItem v-for="warehouse in warehouses" :key="warehouse.id" :value="String(warehouse.id)">
              {{ warehouse.name }}
            </SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto flex items-center gap-3 text-xs text-muted-foreground">
          <span>共 {{ total }} 条库存</span>
          <span v-if="warningCount > 0" class="flex items-center gap-1 rounded-full bg-amber-50 px-2 py-1 font-medium text-amber-600">
            <AlertTriangle class="h-3 w-3" />本页 {{ warningCount }} 项预警
          </span>
        </div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">商品</TableHead>
            <TableHead>仓库</TableHead>
            <TableHead class="text-right">当前库存</TableHead>
            <TableHead class="text-right">安全库存</TableHead>
            <TableHead>库存状态</TableHead>
            <TableHead>更新时间</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="row in rows" :key="row.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-50 text-slate-500">
                  <Boxes class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ row.productName }}</p>
                  <p class="text-[11px] text-slate-400">{{ row.productCode }}</p>
                </div>
              </div>
            </TableCell>
            <TableCell class="text-sm text-slate-600">{{ row.warehouseName }}</TableCell>
            <TableCell class="text-right text-sm font-semibold" :class="row.qty <= (row.safetyStock ?? 0) ? 'text-red-600' : 'text-slate-800'">
              {{ row.qty }} <span class="text-xs font-normal text-slate-400">{{ row.unit }}</span>
            </TableCell>
            <TableCell class="text-right text-sm text-slate-500">{{ row.safetyStock }}</TableCell>
            <TableCell>
              <Badge v-if="row.qty <= (row.safetyStock ?? 0)" variant="destructive">
                <AlertTriangle class="mr-1 h-3 w-3" />库存不足
              </Badge>
              <Badge v-else variant="success">充足</Badge>
            </TableCell>
            <TableCell class="text-xs text-slate-500">{{ row.updateTime }}</TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end">
                <Button variant="ghost" size="sm" @click="openAdjust(row)">调整</Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && rows.length === 0">
            <TableCell colspan="7" class="py-14 text-center text-sm text-muted-foreground">暂无库存数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>库存调整</DialogTitle>
          <DialogDescription>手工增减库存会生成一条库存调整流水</DialogDescription>
        </DialogHeader>

        <div class="space-y-4">
          <div class="space-y-1.5">
            <Label>仓库 <span class="text-red-500">*</span></Label>
            <Select v-model="form.warehouseId">
              <SelectTrigger><SelectValue placeholder="请选择仓库" /></SelectTrigger>
              <SelectContent>
                <SelectItem v-for="warehouse in warehouses" :key="warehouse.id" :value="String(warehouse.id)">
                  {{ warehouse.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5">
            <Label>商品 <span class="text-red-500">*</span></Label>
            <Select v-model="form.productId">
              <SelectTrigger><SelectValue placeholder="请选择商品" /></SelectTrigger>
              <SelectContent>
                <SelectItem v-for="product in products" :key="product.id" :value="String(product.id)">
                  {{ product.name }}（{{ product.code }}）
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1.5">
              <Label>调整类型</Label>
              <Select v-model="form.type">
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="IN">增加库存</SelectItem>
                  <SelectItem value="OUT">扣减库存</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-1.5">
              <Label>数量 <span class="text-red-500">*</span></Label>
              <Input v-model="form.qty" type="number" min="1" />
            </div>
          </div>
          <div class="space-y-1.5">
            <Label>调整原因</Label>
            <Textarea v-model="form.remark" placeholder="如 盘点差异、报损、期初建账" />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="saving" @click="submit">{{ saving ? '提交中...' : '确认调整' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
