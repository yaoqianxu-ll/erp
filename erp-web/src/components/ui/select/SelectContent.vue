<script setup lang="ts">
import {
  SelectContent,
  SelectPortal,
  type SelectContentEmits,
  type SelectContentProps,
  useForwardPropsEmits,
} from 'radix-vue'
import type { HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<SelectContentProps & { class?: HTMLAttributes['class'] }>(),
  { position: 'popper' },
)
const emits = defineEmits<SelectContentEmits>()
const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
  <SelectPortal>
    <SelectContent
      v-bind="forwarded"
      :side-offset="4"
      :class="
        cn(
          'relative z-[80] max-h-72 min-w-[8rem] overflow-hidden rounded-md border bg-popover text-popover-foreground shadow-lg data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
          props.class,
        )
      "
    >
      <div class="max-h-64 overflow-y-auto p-1 scrollbar-thin">
        <slot />
      </div>
    </SelectContent>
  </SelectPortal>
</template>
