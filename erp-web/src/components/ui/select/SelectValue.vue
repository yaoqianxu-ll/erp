<script setup lang="ts">
import { SelectValue, type SelectValueProps } from 'radix-vue'
import { cn } from '@/lib/utils'

const props = defineProps<SelectValueProps & { class?: string }>()
</script>

<template>
  <SelectValue :class="cn('truncate', props.class)" :placeholder="props.placeholder" />
</template>
