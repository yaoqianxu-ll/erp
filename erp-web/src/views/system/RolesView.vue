<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Pencil, Plus, Search, ShieldCheck, Trash2 } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import AppIcon from '@/components/AppIcon.vue'
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
import type { Menu, PageResult, Role } from '@/lib/types'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

interface FlatMenu {
  menu: Menu
  depth: number
}

const roles = ref<Role[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const loading = ref(false)

const menuTree = ref<Menu[]>([])
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  name: '',
  code: '',
  remark: '',
  status: '1',
  menuIds: [] as number[],
})

const flatMenus = computed<FlatMenu[]>(() => {
  const result: FlatMenu[] = []
  const walk = (nodes: Menu[], depth: number) => {
    nodes.forEach((menu) => {
      result.push({ menu, depth })
      if (menu.children?.length) walk(menu.children, depth + 1)
    })
  }
  walk(menuTree.value, 0)
  return result
})

function descendants(menu: Menu): number[] {
  const ids = [menu.id]
  menu.children?.forEach((child) => ids.push(...descendants(child)))
  return ids
}

function isChecked(menu: Menu) {
  return form.menuIds.includes(menu.id)
}

function toggleMenu(menu: Menu, checked: boolean) {
  const ids = descendants(menu)
  if (checked) {
    form.menuIds = Array.from(new Set([...form.menuIds, ...ids]))
  } else {
    form.menuIds = form.menuIds.filter((id) => !ids.includes(id))
  }
}

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<Role>>('/roles', {
      page: page.value,
      size,
      keyword: keyword.value || undefined,
    })
    roles.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadMenus() {
  menuTree.value = await api.get<Menu[]>('/menus/tree')
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', code: '', remark: '', status: '1', menuIds: [] })
  dialogOpen.value = true
}

async function openEdit(role: Role) {
  const detail = await api.get<Role>(`/roles/${role.id}`)
  editingId.value = role.id
  Object.assign(form, {
    name: detail.name,
    code: detail.code,
    remark: detail.remark || '',
    status: String(detail.status),
    menuIds: [...(detail.menuIds || [])],
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.name.trim()) return toast.error('请填写角色名称')
  if (!form.code.trim()) return toast.error('请填写角色编码')
  saving.value = true
  try {
    const payload = { ...form, status: Number(form.status) }
    if (editingId.value) {
      await api.put(`/roles/${editingId.value}`, payload)
      toast.success('角色已更新')
    } else {
      await api.post('/roles', payload)
      toast.success('角色已创建')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(role: Role) {
  const ok = await confirmAction('删除角色', `确定删除角色「${role.name}」吗？已分配用户的角色无法删除。`)
  if (!ok) return
  await api.delete(`/roles/${role.id}`)
  toast.success('角色已删除')
  load()
}

function selectAll() {
  form.menuIds = flatMenus.value.map((item) => item.menu.id)
}

onMounted(() => {
  load()
  loadMenus()
})
</script>

<template>
  <div>
    <PageHeader title="角色管理" description="通过角色控制可访问的菜单与业务模块">
      <template #actions>
        <Button @click="openCreate"><Plus class="h-4 w-4" />新增角色</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center">
        <div class="relative w-full sm:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索角色名称或编码" @keyup.enter="search" />
        </div>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 个角色</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">角色</TableHead>
            <TableHead>角色编码</TableHead>
            <TableHead>备注</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>创建时间</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="role in roles" :key="role.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600">
                  <ShieldCheck class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ role.name }}</p>
                  <p class="text-[11px] text-slate-400">{{ role.menuIds?.length || 0 }} 个菜单权限</p>
                </div>
              </div>
            </TableCell>
            <TableCell><code class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-600">{{ role.code }}</code></TableCell>
            <TableCell class="max-w-56 truncate text-sm text-slate-500">{{ role.remark || '-' }}</TableCell>
            <TableCell>
              <Badge :variant="role.status === 1 ? 'success' : 'secondary'">{{ role.status === 1 ? '启用' : '停用' }}</Badge>
            </TableCell>
            <TableCell class="text-xs text-slate-500">{{ role.createTime }}</TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end gap-1">
                <Button variant="ghost" size="sm" @click="openEdit(role)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(role)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && roles.length === 0">
            <TableCell colspan="6" class="py-14 text-center text-sm text-muted-foreground">暂无角色数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-xl">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑角色' : '新增角色' }}</DialogTitle>
          <DialogDescription>角色决定用户登录后可见的菜单范围</DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>角色名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.name" placeholder="如 采购专员" />
          </div>
          <div class="space-y-1.5">
            <Label>角色编码 <span class="text-red-500">*</span></Label>
            <Input v-model="form.code" placeholder="如 buyer" />
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
          <div class="space-y-1.5">
            <Label>备注</Label>
            <Input v-model="form.remark" placeholder="角色职责说明" />
          </div>
        </div>

        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <Label>菜单权限</Label>
            <div class="flex items-center gap-2 text-xs">
              <button class="text-primary hover:underline" @click="selectAll">全选</button>
              <span class="text-slate-300">|</span>
              <button class="text-slate-500 hover:underline" @click="form.menuIds = []">清空</button>
            </div>
          </div>
          <div class="max-h-72 overflow-y-auto rounded-lg border border-slate-200 p-2 scrollbar-thin">
            <label
              v-for="item in flatMenus"
              :key="item.menu.id"
              class="flex cursor-pointer items-center gap-2.5 rounded-md px-2 py-1.5 text-sm text-slate-700 hover:bg-slate-50"
              :style="{ paddingLeft: `${item.depth * 20 + 8}px` }"
            >
              <input
                type="checkbox"
                class="h-4 w-4 rounded border-slate-300 text-primary focus:ring-primary"
                :checked="isChecked(item.menu)"
                @change="toggleMenu(item.menu, ($event.target as HTMLInputElement).checked)"
              />
              <AppIcon :name="item.menu.icon" class="h-3.5 w-3.5 text-slate-400" />
              <span>{{ item.menu.name }}</span>
              <span v-if="item.menu.type === 0" class="rounded bg-slate-100 px-1.5 py-0.5 text-[10px] text-slate-500">目录</span>
            </label>
          </div>
          <p class="text-[11px] text-slate-400">已选择 {{ form.menuIds.length }} 个菜单，勾选目录会同时勾选其下所有菜单。</p>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="saving" @click="submit">{{ saving ? '保存中...' : '保存' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
