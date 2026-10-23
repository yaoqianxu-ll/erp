<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ArrowDownLeft, ArrowUpRight, Search } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Table from '@/components/ui/table/Table.vue'
import TableHeader from '@/components/ui/table/TableHeader.vue'
import TableBody from '@/components/ui/table/TableBody.vue'
import TableRow from '@/components/ui/table/TableRow.vue'
import TableHead from '@/components/ui/table/TableHead.vue'
import TableCell from '@/components/ui/table/TableCell.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import { api } from '@/lib/http'
import type { PageResult, StockRecord } from '@/lib/types'

const records = ref<StockRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const typeFilter = ref('ALL')
const keyword = ref('')
const loading = ref(false)

const bizLabels: Record<string, string> = {
  PURCHASE: '采购入库',
  SALES: '销售出库',
  ADJUST: '库存调整',
}

async function load() {
  loading.value = true
  try {
    const result = await api.get<PageResult<StockRecord>>('/inventory/records', {
      page: page.value,
      size,
      type: typeFilter.value === 'ALL' ? undefined : typeFilter.value,
      keyword: keyword.value || undefined,
    })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

watch(typeFilter, () => {
  page.value = 1
  load()
})

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="出入库记录" description="所有库存变动均可追溯，支持按类型与关键字筛选" />

    <Card>
      <div class="flex flex-col gap-3 border-b border-slate-100 p-4 sm:flex-row sm:items-center">
        <div class="relative w-full sm:max-w-xs">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input v-model="keyword" class="pl-9" placeholder="搜索商品或业务单号" @keyup.enter="search" />
        </div>
        <Select v-model="typeFilter">
          <SelectTrigger class="w-full sm:w-32"><SelectValue placeholder="全部类型" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">全部类型</SelectItem>
            <SelectItem value="IN">入库</SelectItem>
            <SelectItem value="OUT">出库</SelectItem>
          </SelectContent>
        </Select>
        <Button variant="outline" @click="search">查询</Button>
        <div class="ml-auto text-xs text-muted-foreground">共 {{ total }} 条流水</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="pl-5">时间</TableHead>
            <TableHead>类型</TableHead>
            <TableHead>商品</TableHead>
            <TableHead>仓库</TableHead>
            <TableHead class="text-right">数量</TableHead>
            <TableHead>业务来源</TableHead>
            <TableHead>单号 / 备注</TableHead>
            <TableHead class="pr-5">操作人</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="record in records" :key="record.id">
            <TableCell class="pl-5 text-xs text-slate-500">{{ record.createTime }}</TableCell>
            <TableCell>
              <Badge :variant="record.type === 'IN' ? 'success' : 'warning'">
                <ArrowDownLeft v-if="record.type === 'IN'" class="mr-1 h-3 w-3" />
                <ArrowUpRight v-else class="mr-1 h-3 w-3" />
                {{ record.type === 'IN' ? '入库' : '出库' }}
              </Badge>
            </TableCell>
            <TableCell>
              <p class="font-medium text-slate-800">{{ record.productName }}</p>
              <p class="text-[11px] text-slate-400">{{ record.productCode }}</p>
            </TableCell>
            <TableCell class="text-sm text-slate-600">{{ record.warehouseName }}</TableCell>
            <TableCell class="text-right text-sm font-medium" :class="record.type === 'IN' ? 'text-emerald-600' : 'text-amber-600'">
              {{ record.type === 'IN' ? '+' : '-' }}{{ record.qty }} {{ record.unit }}
            </TableCell>
            <TableCell class="text-sm text-slate-600">{{ bizLabels[record.bizType || ''] || record.bizType || '-' }}</TableCell>
            <TableCell>
              <p class="text-xs text-slate-600">{{ record.bizNo || '-' }}</p>
              <p v-if="record.remark" class="text-[11px] text-slate-400">{{ record.remark }}</p>
            </TableCell>
            <TableCell class="pr-5 text-sm text-slate-600">{{ record.createBy || '-' }}</TableCell>
          </TableRow>
          <TableRow v-if="!loading && records.length === 0">
            <TableCell colspan="8" class="py-14 text-center text-sm text-muted-foreground">暂无流水记录</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Pagination :page="page" :size="size" :total="total" @update:page="(p) => { page = p; load() }" />
    </Card>
  </div>
</template>
