<script setup lang="ts">
import { computed } from 'vue'
import Badge from '@/components/ui/badge/Badge.vue'

const props = defineProps<{ status: string }>()

type Variant = 'default' | 'secondary' | 'destructive' | 'outline' | 'success' | 'warning'

const map: Record<string, { label: string; variant: Variant }> = {
  PENDING: { label: '待审核', variant: 'warning' },
  APPROVED: { label: '已审核', variant: 'default' },
  RECEIVED: { label: '已入库', variant: 'success' },
  SHIPPED: { label: '已出库', variant: 'success' },
  CANCELLED: { label: '已取消', variant: 'secondary' },
}

const config = computed(() => map[props.status] || { label: props.status, variant: 'secondary' as Variant })
</script>

<template>
  <Badge :variant="config.variant">{{ config.label }}</Badge>
</template>
