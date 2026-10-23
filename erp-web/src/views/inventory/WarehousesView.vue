<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { MapPin, Pencil, Plus, Search, Trash2, Warehouse } from 'lucide-vue-next'
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
import { api } from '@/lib/http'
import type { PageResult, Warehouse as WarehouseType } from '@/lib/types'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

const warehouses = ref<WarehouseType[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const loading = ref(false)

const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  code: '',
  name: '',
  address: '',
  manager: '',
  phone: '',
  status: '1',
})

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<WarehouseType>>('/warehouses', {
      page: page.value,
      size,
      keyword: keyword.value || undefined,
    })
    warehouses.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { code: '', name: '', address: '', manager: '', phone: '', status: '1' })
  dialogOpen.value = true
}

function openEdit(warehouse: WarehouseType) {
  editingId.value = warehouse.id
  Object.assign(form, {
    code: warehouse.code,
    name: warehouse.name,
    address: warehouse.address || '',
    manager: warehouse.manager || '',
    phone: warehouse.phone || '',
    status: String(warehouse.status),
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.code.trim()) return toast.error('请填写仓库编码')
  if (!form.name.trim()) return toast.error('请填写仓库名称')
  saving.value = true
  try {
    const payload = { ...form, status: Number(form.status) }
    if (editingId.value) {
      await api.put(`/warehouses/${editingId.value}`, payload)
      toast.success('仓库已更新')
    } else {
      await api.post('/warehouses', payload)
      toast.success('仓库已创建')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(warehouse: WarehouseType) {
  const ok = await confirmAction('删除仓库', `确定删除仓库「${warehouse.name}」吗？仓库存在库存时无法删除。`)
  if (!ok) return
  await api.delete(`/warehouses/${warehouse.id}`)
  toast.success('仓库已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="仓库管理" description="维护仓库档案、负责人与启用状态">
      <template #actions>
        <Button @click="openCreate"><Plus class="h-4 w-4" />新增仓库</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center">
        <div class="relative w-full sm:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索仓库名称或编码" @keyup.enter="search" />
        </div>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 个仓库</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">仓库</TableHead>
            <TableHead>地址</TableHead>
            <TableHead>负责人</TableHead>
            <TableHead>联系电话</TableHead>
            <TableHead>状态</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="warehouse in warehouses" :key="warehouse.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-cyan-50 text-cyan-600">
                  <Warehouse class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ warehouse.name }}</p>
                  <p class="text-[11px] text-slate-400">{{ warehouse.code }}</p>
                </div>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex items-start gap-1 text-sm text-slate-600">
                <MapPin class="mt-0.5 h-3.5 w-3.5 shrink-0 text-slate-400" />
                <span class="max-w-64">{{ warehouse.address || '-' }}</span>
              </div>
            </TableCell>
            <TableCell class="text-sm text-slate-600">{{ warehouse.manager || '-' }}</TableCell>
            <TableCell class="text-sm text-slate-600">{{ warehouse.phone || '-' }}</TableCell>
            <TableCell>
              <Badge :variant="warehouse.status === 1 ? 'success' : 'secondary'">{{ warehouse.status === 1 ? '启用' : '停用' }}</Badge>
            </TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end gap-1">
                <Button variant="ghost" size="sm" @click="openEdit(warehouse)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(warehouse)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && warehouses.length === 0">
            <TableCell colspan="6" class="py-14 text-center text-sm text-muted-foreground">暂无仓库数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑仓库' : '新增仓库' }}</DialogTitle>
          <DialogDescription>仓库编码需唯一，停用后不再出现在业务单据的下拉选项中</DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>仓库编码 <span class="text-red-500">*</span></Label>
            <Input v-model="form.code" placeholder="如 WH-SZ" />
          </div>
          <div class="space-y-1.5">
            <Label>仓库名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.name" placeholder="如 深圳分仓" />
          </div>
          <div class="space-y-1.5 sm:col-span-2">
            <Label>仓库地址</Label>
            <Input v-model="form.address" placeholder="省市区 + 详细地址" />
          </div>
          <div class="space-y-1.5">
            <Label>负责人</Label>
            <Input v-model="form.manager" placeholder="如 王建国" />
          </div>
          <div class="space-y-1.5">
            <Label>联系电话</Label>
            <Input v-model="form.phone" placeholder="11 位手机号" />
          </div>
          <div class="space-y-1.5">
            <Label>状态</Label>
            <Select v-model="form.status">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="1">启用</SelectItem>
                <SelectItem value="0">停用</SelectItem>
              </SelectContent>
            </Select>
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
