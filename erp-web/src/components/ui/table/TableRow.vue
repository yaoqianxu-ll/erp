<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'
const props = defineProps<{ class?: HTMLAttributes['class'] }>()
</script>
<template>
  <tr :class="cn('border-b border-slate-100 transition-colors hover:bg-slate-50/70', props.class)"><slot /></tr>
</template>
