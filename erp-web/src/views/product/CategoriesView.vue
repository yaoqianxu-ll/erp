<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { FolderTree, Pencil, Plus, Trash2 } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Label from '@/components/ui/label/Label.vue'
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
import type { Category } from '@/lib/types'
import { toast } from '@/lib/toast'
import { confirmAction } from '@/lib/confirm'

interface FlatCategory {
  category: Category
  depth: number
}

const tree = ref<Category[]>([])
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)

const form = reactive({ name: '', parentId: '0', sort: 0 })

const flatCategories = computed<FlatCategory[]>(() => {
  const result: FlatCategory[] = []
  const walk = (nodes: Category[], depth: number) => {
    nodes.forEach((category) => {
      result.push({ category, depth })
      if (category.children?.length) walk(category.children, depth + 1)
    })
  }
  walk(tree.value, 0)
  return result
})

async function load() {
  tree.value = await api.get<Category[]>('/categories/tree')
}

function openCreate(parent?: Category) {
  editingId.value = null
  Object.assign(form, { name: '', parentId: parent ? String(parent.id) : '0', sort: 0 })
  dialogOpen.value = true
}

function openEdit(category: Category) {
  editingId.value = category.id
  Object.assign(form, {
    name: category.name,
    parentId: String(category.parentId || 0),
    sort: category.sort ?? 0,
  })
  dialogOpen.value = true
}

async function submit() {
  if (!form.name.trim()) return toast.error('请填写分类名称')
  saving.value = true
  try {
    const payload = { name: form.name, parentId: Number(form.parentId), sort: Number(form.sort) }
    if (editingId.value) {
      await api.put(`/categories/${editingId.value}`, payload)
      toast.success('分类已更新')
    } else {
      await api.post('/categories', payload)
      toast.success('分类已创建')
    }
    dialogOpen.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(category: Category) {
  const ok = await confirmAction('删除分类', `确定删除分类「${category.name}」吗？分类下有商品或子分类时无法删除。`)
  if (!ok) return
  await api.delete(`/categories/${category.id}`)
  toast.success('分类已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="商品分类" description="两级分类结构，便于商品归档与筛选">
      <template #actions>
        <Button @click="openCreate()"><Plus class="h-4 w-4" />新增分类</Button>
      </template>
    </PageHeader>

    <Card>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">分类名称</TableHead>
            <TableHead>层级</TableHead>
            <TableHead>排序</TableHead>
            <TableHead>创建时间</TableHead>
            <TableHead class="pr-5 text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in flatCategories" :key="item.category.id">
            <TableCell class="pl-5">
              <div class="flex items-center gap-2.5" :style="{ paddingLeft: `${item.depth * 24}px` }">
                <div class="flex h-7 w-7 items-center justify-center rounded-md bg-amber-50 text-amber-600">
                  <FolderTree class="h-3.5 w-3.5" />
                </div>
                <span class="font-medium text-slate-800">{{ item.category.name }}</span>
              </div>
            </TableCell>
            <TableCell class="text-sm text-slate-500">{{ item.depth === 0 ? '一级分类' : '二级分类' }}</TableCell>
            <TableCell class="text-sm text-slate-500">{{ item.category.sort }}</TableCell>
            <TableCell class="text-xs text-slate-500">{{ item.category.createTime }}</TableCell>
            <TableCell class="pr-5">
              <div class="flex justify-end gap-1">
                <Button v-if="item.depth === 0" variant="ghost" size="sm" @click="openCreate(item.category)">
                  <Plus class="h-3.5 w-3.5" />子分类
                </Button>
                <Button variant="ghost" size="sm" @click="openEdit(item.category)"><Pencil class="h-3.5 w-3.5" />编辑</Button>
                <Button variant="ghost" size="sm" class="text-red-600 hover:bg-red-50 hover:text-red-600" @click="remove(item.category)">
                  <Trash2 class="h-3.5 w-3.5" />删除
                </Button>
              </div>
            </TableCell>
          </TableRow>
          <TableRow v-if="flatCategories.length === 0">
            <TableCell colspan="5" class="py-14 text-center text-sm text-muted-foreground">暂无分类数据</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </Card>

    <Dialog v-model:open="dialogOpen">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>{{ editingId ? '编辑分类' : '新增分类' }}</DialogTitle>
          <DialogDescription>分类最多两级，商品挂在具体分类下</DialogDescription>
        </DialogHeader>

        <div class="space-y-4">
          <div class="space-y-1.5">
            <Label>分类名称 <span class="text-red-500">*</span></Label>
            <Input v-model="form.name" placeholder="如 电脑外设" />
          </div>
          <div class="space-y-1.5">
            <Label>上级分类</Label>
            <Select v-model="form.parentId">
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="0">顶级分类</SelectItem>
                <SelectItem v-for="item in flatCategories.filter((i) => i.depth === 0)" :key="item.category.id" :value="String(item.category.id)">
                  {{ item.category.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-1.5">
            <Label>排序</Label>
            <Input v-model.number="form.sort" type="number" min="0" />
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
