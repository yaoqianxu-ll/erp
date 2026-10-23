<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { KeyRound, Pencil, Plus, Search, Trash2, UserRound } from 'lucide-vue-next'
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
import type { PageResult, Role, User } from '@/lib/types'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

const users = ref<User[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const statusFilter = ref('ALL')
const loading = ref(false)

const roles = ref<Role[]>([])
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  password: '',
  status: '1',
  roleIds: [] as number[],
})

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<User>>('/users', {
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: statusFilter.value === 'ALL' ? undefined : Number(statusFilter.value),
    })
    users.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  roles.value = await api.get<Role[]>('/roles/all')
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    username: '',
    nickname: '',
    phone: '',
    email: '',
    password: '',
    status: '1',
    roleIds: [],
  })
  dialogOpen.value = true
}

function openEdit(user: User) {
  editingId.value = user.id
  Object.assign(form, {
    username: user.username,
    nickname: user.nickname,
    phone: user.phone || '',
    email: user.email || '',
    password: '',
    status: String(user.status),
    roleIds: [...(user.roleIds || [])],
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.username.trim()) return toast.error('请输入登录账号')
  if (!form.nickname.trim()) return toast.error('请输入姓名')
  saving.value = true
  try {
    const payload = {
      ...form,
      status: Number(form.status),
      password: form.password || null,
    }
    if (editingId.value) {
      await api.put(`/users/${editingId.value}`, payload)
      toast.success('用户已更新')
    } else {
      await api.post('/users', payload)
      toast.success('用户已创建，初始密码为 123456')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(user: User) {
  const ok = await confirmAction('删除用户', `确定删除用户「${user.nickname}」吗？该操作不可恢复。`)
  if (!ok) return
  await api.delete(`/users/${user.id}`)
  toast.success('用户已删除')
  load()
}

async function resetPassword(user: User) {
  const ok = await confirmAction(
    '重置密码',
    `将用户「${user.nickname}」的密码重置为 123456？`,
    '确认重置',
  )
  if (!ok) return
  await api.post(`/users/${user.id}/reset-password`)
  toast.success('密码已重置为 123456')
}

async function toggleStatus(user: User) {
  const next = user.status === 1 ? 0 : 1
  await api.patch(`/users/${user.id}/status?status=${next}`)
  toast.success(next === 1 ? '账号已启用' : '账号已停用')
  load()
}

watch(statusFilter, () => {
  page.value = 1
  load()
})

onMounted(() => {
  load()
  loadRoles()
})
</script>

<template>
  <div>
    <PageHeader title="用户管理" description="维护系统账号、所属角色与启用状态">
      <template #actions>
        <Button @click="openCreate"><Plus class="h-4 w-4" />新增用户</Button>
      </template>
    </PageHeader>

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center">
        <div class="relative w-full sm:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索账号、姓名或手机号" @keyup.enter="search" />
        </div>
        <Select v-model="statusFilter">
          <SelectTrigger class="w-full sm:w-32"><SelectValue placeholder="全部状态" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部状态</SelectItem>
            <SelectItem value="1">启用</SelectItem>
            <SelectItem value="0">停用</SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 个账号</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">用户</TableHead>
            <TableHead>角色</TableHead>
            <TableHead>联系方式</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>创建时间</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="user in users" :key="user.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-full bg-blue-50 text-blue-600">
                  <UserRound class="h-4 w-4" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ user.nickname }}</p>
                  <p class="text-[11px] text-slate-400">@{{ user.username }}</p>
                </div>
              </div>
            </TableCell>
            <TableCell>
              <div class="flex flex-wrap gap-1">
                <Badge
                  v-for="roleId in user.roleIds || []"
                  :key="roleId"
                  variant="secondary"
                >
                  {{ roles.find((r) => r.id === roleId)?.name || '未知角色' }}
                </Badge>
                <span v-if="!user.roleIds?.length" class="text-xs text-slate-400">未分配</span>
              </div>
            </TableCell>
            <TableCell>
              <p class="text-sm text-slate-600">{{ user.phone || '-' }}</p>
              <p class="text-[11px] text-slate-400">{{ user.email || '-' }}</p>
            </TableCell>
            <TableCell>
              <button @click="toggleStatus(user)">
                <Badge :variant="user.status === 1 ? 'success' : 'secondary'" class="cursor-pointer">
                  {{ user.status === 1 ? '启用' : '停用' }}
                </Badge>
              </button>
            </TableCell>
            <TableCell class="text-xs text-slate-500">{{ user.createTime }}</TableCell>
            <TableCell class="pr-5">
              <div class="flex items-center justify-end gap-1">
                <Button variant="ghost" size="sm" @click="openEdit(user)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" @click="resetPassword(user)"><KeyRound class="h-3.5 w-3.5" />重置</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(user)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="!loading && users.length === 0">
            <TableCell colspan="6" class="py-14 text-center text-sm text-muted-foreground">暂无用户数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑用户' : '新增用户' }}</DialogTitle>
          <DialogDescription>
            {{ editingId ? '修改账号基本信息与角色分配' : '创建账号，未填写密码时默认为 123456' }}
          </DialogDescription>
        </DialogHeader>

        <div class="grid gap-4 sm:grid-cols-2">
          <div class="space-y-1.5">
            <Label>登录账号 <span class="text-red-500">*</span></Label>
            <Input v-model="form.username" placeholder="如 zhangsan" />
          </div>
          <div class="space-y-1.5">
            <Label>姓名 <span class="text-red-500">*</span></Label>
            <Input v-model="form.nickname" placeholder="如 张三" />
          </div>
          <div class="space-y-1.5">
            <Label>手机号</Label>
            <Input v-model="form.phone" placeholder="11 位手机号" />
          </div>
          <div class="space-y-1.5">
            <Label>邮箱</Label>
            <Input v-model="form.email" placeholder="name@example.com" />
          </div>
          <div class="space-y-1.5">
            <Label>{{ editingId ? '新密码（可选）' : '初始密码' }}</Label>
            <Input v-model="form.password" type="password" :placeholder="editingId ? '留空则不修改' : '默认 123456'" />
          </div>
          <div class="space-y-1.5">
            <Label>账号状态</Label>
            <Select v-model="form.status">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="1">启用</SelectItem>
                <SelectItem value="0">停用</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2 sm:col-span-2">
            <Label>分配角色</Label>
            <div class="flex flex-wrap gap-3 rounded-lg border border-slate-200 p-3">
              <label v-for="role in roles" :key="role.id" class="flex items-center gap-2 text-sm text-slate-700">
                <input
                  v-model="form.roleIds"
                  type="checkbox"
                  :value="role.id"
                  class="h-4 w-4 rounded border-slate-300 text-primary focus:ring-primary"
                />
                {{ role.name }}
              </label>
              <span v-if="roles.length === 0" class="text-xs text-slate-400">暂无可分配角色</span>
            </div>
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
