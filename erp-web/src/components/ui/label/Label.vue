<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
</script>

<template>
  <label :class="cn('text-sm font-medium leading-none text-slate-700', props.class)" v-bind="$attrs">
    <slot />
  </label>
</template>
