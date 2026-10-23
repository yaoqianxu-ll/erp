<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { FolderTree, Pencil, Plus, Trash2 } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import AppIcon from '@/components/AppIcon.vue'
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
import type { Menu } from '@/lib/types'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

interface FlatMenu {
  menu: Menu
  depth: number
}

const menuTree = ref<Menu[]>([])
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  parentId: '0',
  name: '',
  path: '',
  icon: '',
  type: '1',
  perms: '',
  sort: 0,
  visible: '1',
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

const parentOptions = computed(() => flatMenus.value.filter((item) => item.menu.type === 0))

async function load() {
  menuTree.value = await api.get<Menu[]>('/menus/tree')
}

function openCreate(parent?: Menu) {
  editingId.value = null
  Object.assign(form, {
    parentId: parent ? String(parent.id) : '0',
    name: '',
    path: '',
    icon: '',
    type: parent ? '1' : '0',
    perms: '',
    sort: 0,
    visible: '1',
  })
  dialogOpen.value = true
}

function openEdit(menu: Menu) {
  editingId.value = menu.id
  Object.assign(form, {
    parentId: String(menu.parentId || 0),
    name: menu.name,
    path: menu.path || '',
    icon: menu.icon || '',
    type: String(menu.type),
    perms: menu.perms || '',
    sort: menu.sort ?? 0,
    visible: String(menu.visible ?? 1),
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.name.trim()) return toast.error('请填写菜单名称')
  saving.value = true
  try {
    const payload = {
      ...form,
      parentId: Number(form.parentId),
      type: Number(form.type),
      sort: Number(form.sort),
      visible: Number(form.visible),
    }
    if (editingId.value) {
      await api.put(`/menus/${editingId.value}`, payload)
      toast.success('菜单已更新')
    } else {
      await api.post('/menus', payload)
      toast.success('菜单已创建')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(menu: Menu) {
  const ok = await confirmAction('删除菜单', `确定删除「${menu.name}」吗？请先确保没有子菜单。`)
  if (!ok) return
  await api.delete(`/menus/${menu.id}`)
  toast.success('菜单已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="菜单管理" description="配置侧边栏导航结构，角色授权时按菜单分配">
      <template #actions>
        <Button @click="openCreate()"><Plus class="h-4 w-4" />新增菜单</Button>
      </template>
    </PageHeader>

    <Card>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">菜单名称</TableHead>
            <TableHead>路由地址</TableHead>
            <TableHead>权限标识</TableHead>
            <TableHead>类型</TableHead>
            <TableHead class="text-center">排序</TableHead>
            <TableHead>可见</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in flatMenus" :key="item.menu.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-2" :style="{ paddingLeft: `${item.depth * 22}px` }">
                <div class="flex h-7 w-7 items-center justify-center rounded-md bg-slate-50 text-slate-500">
                  <AppIcon :name="item.menu.icon" class="h-3.5 w-3.5" />
                </div>
                <span class="font-medium text-slate-800">{{ item.menu.name }}</span>
              </div>
            </TableCell>
            <TableCell><code class="text-xs text-slate-500">{{ item.menu.path || '-' }}</code></TableCell>
            <TableCell><code class="text-xs text-slate-500">{{ item.menu.perms || '-' }}</code></TableCell>
            <TableCell>
              <Badge :variant="item.menu.type === 0 ? 'secondary' : 'default'">
                {{ item.menu.type === 0 ? '目录' : '菜单' }}
              </Badge>
            </TableCell>
            <TableCell class="text-center text-sm text-slate-500">{{ item.menu.sort }}</TableCell>
            <TableCell>
              <Badge :variant="item.menu.visible === 1 ? 'success' : 'secondary'">
                {{ item.menu.visible === 1 ? '显示' : '隐藏' }}
              </Badge>
            </TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end gap-1">
                <Button v-if="item.menu.type === 0" variant="ghost" size="sm" @click="openCreate(item.menu)">
                  <Plus class="h-3.5 w-3.5" />子菜单
                </Button>
                <Button variant="ghost" size="sm" @click="openEdit(item.menu)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(item.menu)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="flatMenus.length === 0">
            <TableCell colspan="7" class="py-14 text-center text-sm text-muted-foreground">暂无菜单数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑菜单' : '新增菜单' }}</DialogTitle>
          <DialogDescription>目录用于分组，菜单对应一个具体页面</DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>上级菜单</Label>
            <Select v-model="form.parentId">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="0">顶级菜单</SelectItem>
                <SelectItem v-for="item in parentOptions" :key="item.menu.id" :value="String(item.menu.id)">
                  {{ '　'.repeat(item.depth) }}{{ item.menu.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5">
            <Label>菜单类型</Label>
            <Select v-model="form.type">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="0">目录</SelectItem>
                <SelectItem value="1">菜单</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5">
            <Label>菜单名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.name" placeholder="如 采购订单" />
          </div>
          <div class="space-y-1.5">
            <Label>路由地址</Label>
            <Input v-model="form.path" placeholder="如 /purchase/orders" />
          </div>
          <div class="space-y-1.5">
            <Label>图标（lucide 名称）</Label>
            <Input v-model="form.icon" placeholder="如 ShoppingCart" />
          </div>
          <div class="space-y-1.5">
            <Label>权限标识</Label>
            <Input v-model="form.perms" placeholder="如 purchase:order:list" />
          </div>
          <div class="space-y-1.5">
            <Label>排序</Label>
            <Input v-model.number="form.sort" type="number" min="0" />
          </div>
          <div class="space-y-1.5">
            <Label>是否显示</Label>
            <Select v-model="form.visible">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="1">显示</SelectItem>
                <SelectItem value="0">隐藏</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="flex items-start gap-2 rounded-lg bg-blue-50/60 p-3 text-[11px] leading-relaxed text-blue-700">
          <FolderTree class="mt-0.5 h-3.5 w-3.5 shrink-0" />
          <span>图标可选：LayoutDashboard、Users、ShieldCheck、ListTree、Package、Tags、Boxes、ArrowLeftRight、Warehouse、ShoppingCart、Truck。</span>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="saving" @click="submit">{{ saving ? '保存中...' : '保存' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
