<script setup lang="ts">
import { cva, type VariantProps } from 'class-variance-authority'
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const badgeVariants = cva(
  'inline-flex items-center whitespace-nowrap rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary/10 text-primary',
        secondary: 'border-transparent bg-slate-100 text-slate-700',
        destructive: 'border-transparent bg-red-50 text-red-600',
        outline: 'border-slate-200 text-slate-600',
        success: 'border-transparent bg-emerald-50 text-emerald-600',
        warning: 'border-transparent bg-amber-50 text-amber-600',
      },
    },
    defaultVariants: { variant: 'default' },
  },
)

const props = defineProps<{
  variant?: VariantProps<typeof badgeVariants>['variant']
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <span :class="cn(badgeVariants({ variant: props.variant }), props.class)">
    <slot />
  </span>
</template>
