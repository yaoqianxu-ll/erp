<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Package, Pencil, Plus, Search, Trash2 } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Label from '@/components/ui/label/Label.vue'
import Badge from '@/components/ui/badge/Badge.vue'
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
import type { Category, PageResult, Product } from '@/lib/types'
import { money } from '@/lib/format'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

interface FlatCategory {
  category: Category
  depth: number
}

const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const categoryFilter = ref('ALL')
const statusFilter = ref('ALL')
const loading = ref(false)

const categories = ref<Category[]>([])
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  code: '',
  name: '',
  categoryId: '0',
  spec: '',
  unit: '件',
  purchasePrice: '',
  salePrice: '',
  safetyStock: '',
  status: '1',
  remark: '',
})

const flatCategories = computed<FlatCategory[]>(() => {
  const result: FlatCategory[] = []
  const walk = (nodes: Category[], depth: number) => {
    nodes.forEach((category) => {
      result.push({ category, depth })
      if (category.children?.length) walk(category.children, depth + 1)
    })
  }
  walk(categories.value, 0)
  return result
})

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<Product>>('/products', {
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      categoryId: categoryFilter.value === 'ALL' ? undefined : Number(categoryFilter.value),
      status: statusFilter.value === 'ALL' ? undefined : Number(statusFilter.value),
    })
    products.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await api.get<Category[]>('/categories/tree')
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    code: '',
    name: '',
    categoryId: '0',
    spec: '',
    unit: '件',
    purchasePrice: '',
    salePrice: '',
    safetyStock: '',
    status: '1',
    remark: '',
  })
  dialogOpen.value = true
}

function openEdit(product: Product) {
  editingId.value = product.id
  Object.assign(form, {
    code: product.code,
    name: product.name,
    categoryId: product.categoryId ? String(product.categoryId) : '0',
    spec: product.spec || '',
    unit: product.unit || '件',
    purchasePrice: String(product.purchasePrice ?? ''),
    salePrice: String(product.salePrice ?? ''),
    safetyStock: String(product.safetyStock ?? ''),
    status: String(product.status),
    remark: product.remark || '',
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.code.trim()) return toast.error('请填写商品编码')
  if (!form.name.trim()) return toast.error('请填写商品名称')
  saving.value = true
  try {
    const payload = {
      ...form,
      categoryId: form.categoryId === '0' ? null : Number(form.categoryId),
      purchasePrice: Number(form.purchasePrice || 0),
      salePrice: Number(form.salePrice || 0),
      safetyStock: Number(form.safetyStock || 0),
      status: Number(form.status),
    }
    if (editingId.value) {
      await api.put(`/products/${editingId.value}`, payload)
      toast.success('商品已更新')
    } else {
      await api.post('/products', payload)
      toast.success('商品已创建')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(product: Product) {
  const ok = await confirmAction('删除商品', `确定删除商品「${product.name}」吗？历史库存与单据不会自动清理。`)
  if (!ok) return
  await api.delete(`/products/${product.id}`)
  toast.success('商品已删除')
  load()
}

watch([categoryFilter, statusFilter], () => {
  page.value = 1
  load()
})

onMounted(() => {
  load()
  loadCategories()
})
</script>

<template>
  <div>
    <PageHeader title="商品档案" description="维护商品编码、分类、价格与安全库存">
      <template #actions>
        <Button @click="openCreate"><Plus class="h-4 w-4" />新增商品</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 lg:flex-row lg:items-center">
        <div class="relative w-full lg:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索商品名称或编码" @keyup.enter="search" />
        </div>
        <Select v-model="categoryFilter">
          <SelectTrigger class="w-full lg:w-40"><SelectValue placeholder="全部分类" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部分类</SelectItem>
            <SelectItem v-for="item in flatCategories" :key="item.category.id" :value="String(item.category.id)">
              {{ '　'.repeat(item.depth) }}{{ item.category.name }}
            </SelectItem>
          </SelectContent>
        </Select>
        <Select v-model="statusFilter">
          <SelectTrigger class="w-full lg:w-32"><SelectValue placeholder="全部状态" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部状态</SelectItem>
            <SelectItem value="1">在售</SelectItem>
            <SelectItem value="0">停用</SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 个商品</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">商品</TableHead>
            <TableHead>分类</TableHead>
            <TableHead>规格 / 单位</TableHead>
            <TableHead class="text-right">采购价</TableHead>
            <TableHead class="text-right">销售价</TableHead>
            <TableHead class="text-center">安全库存</TableHead>
            <TableHead>状态</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="product in products" :key="product.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
                  <Package class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ product.name }}</p>
                  <p class="text-[11px] text-slate-400">{{ product.code }}</p>
                </div>
              </div>
            </TableCell>
            <TableCell class="text-sm text-slate-600">{{ product.categoryName || '-' }}</TableCell>
            <TableCell>
              <p class="text-sm text-slate-600">{{ product.spec || '-' }}</p>
              <p class="text-[11px] text-slate-400">单位：{{ product.unit || '件' }}</p>
            </TableCell>
            <TableCell class="text-right text-sm text-slate-600">{{ money(product.purchasePrice) }}</TableCell>
            <TableCell class="text-right text-sm font-medium text-slate-800">{{ money(product.salePrice) }}</TableCell>
            <TableCell class="text-center text-sm text-slate-600">{{ product.safetyStock }}</TableCell>
            <TableCell>
              <Badge :variant="product.status === 1 ? 'success' : 'secondary'">{{ product.status === 1 ? '在售' : '停用' }}</Badge>
            </TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end gap-1">
                <Button variant="ghost" size="sm" @click="openEdit(product)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(product)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && products.length === 0">
            <TableCell colspan="8" class="py-14 text-center text-sm text-muted-foreground">暂无商品数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑商品' : '新增商品' }}</DialogTitle>
          <DialogDescription>商品编码需唯一，价格将作为采购/销售的默认单价</DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>商品编码 <span class="text-red-500">*</span></Label>
            <Input v-model="form.code" placeholder="如 P1005" />
          </div>
          <div class="space-y-1.5">
            <Label>商品名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.name" placeholder="如 机械键盘" />
          </div>
          <div class="space-y-1.5">
            <Label>商品分类</Label>
            <Select v-model="form.categoryId">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="0">未分类</SelectItem>
                <SelectItem v-for="item in flatCategories" :key="item.category.id" :value="String(item.category.id)">
                  {{ '　'.repeat(item.depth) }}{{ item.category.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5">
            <Label>规格型号</Label>
            <Input v-model="form.spec" placeholder="如 87键 白色" />
          </div>
          <div class="space-y-1.5">
            <Label>计量单位</Label>
            <Input v-model="form.unit" placeholder="件 / 台 / 箱" />
          </div>
          <div class="space-y-1.5">
            <Label>安全库存</Label>
            <Input v-model="form.safetyStock" type="number" min="0" placeholder="低于该值触发预警" />
          </div>
          <div class="space-y-1.5">
            <Label>采购价（元）</Label>
            <Input v-model="form.purchasePrice" type="number" min="0" step="0.01" placeholder="0.00" />
          </div>
          <div class="space-y-1.5">
            <Label>销售价（元）</Label>
            <Input v-model="form.salePrice" type="number" min="0" step="0.01" placeholder="0.00" />
          </div>
          <div class="space-y-1.5">
            <Label>状态</Label>
            <Select v-model="form.status">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="1">在售</SelectItem>
                <SelectItem value="0">停用</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5 sm:col-span-2">
            <Label>备注</Label>
            <Textarea v-model="form.remark" placeholder="选填，记录商品补充说明" />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="saving" @click="submit">{{ saving ? '保存中...' : '保存' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
