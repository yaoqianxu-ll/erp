<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'

const props = defineProps<{ page: number; size: number; total: number }>()
const emits = defineEmits<{ 'update:page': [value: number] }>()

const pages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
const start = computed(() => (props.total === 0 ? 0 : (props.page - 1) * props.size + 1))
const end = computed(() => Math.min(props.page * props.size, props.total))

function go(page: number) {
  if (page < 1 || page > pages.value) return
  emits('update:page', page)
}
</script>

<template>
  <div class="flex flex-col gap-3 border-t border-slate-100 px-5 py-3 sm:flex-row sm:items-center sm:justify-between">
    <p class="text-xs text-muted-foreground">
      共 {{ total }} 条，当前 {{ start }}-{{ end }} 条
    </p>
    <div class="flex items-center gap-2">
      <Button variant="outline" size="sm" :disabled="page <= 1" @click="go(page - 1)">
        <ChevronLeft class="h-4 w-4" />上一页
      </Button>
      <span class="min-w-16 text-center text-xs text-slate-600">{{ page }} / {{ pages }}</span>
      <Button variant="outline" size="sm" :disabled="page >= pages" @click="go(page + 1)">
        下一页<ChevronRight class="h-4 w-4" />
      </Button>
    </div>
  </div>
</template>
