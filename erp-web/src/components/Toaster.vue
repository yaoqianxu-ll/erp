<script setup lang="ts">
import { CheckCircle2, Info, XCircle } from 'lucide-vue-next'
import { toastState } from '@/lib/toast'
</script>

<template>
  <div class="pointer-events-none fixed right-4 top-4 z-[100] flex w-80 flex-col gap-2">
    <TransitionGroup
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="translate-x-4 opacity-0"
      enter-to-class="translate-x-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-for="item in toastState.items"
        :key="item.id"
        class="pointer-events-auto flex items-start gap-2.5 rounded-lg border bg-white px-4 py-3 text-sm shadow-lg"
      >
        <CheckCircle2 v-if="item.type === 'success'" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-500" />
        <XCircle v-else-if="item.type === 'error'" class="mt-0.5 h-4 w-4 shrink-0 text-red-500" />
        <Info v-else class="mt-0.5 h-4 w-4 shrink-0 text-blue-500" />
        <span class="text-slate-700">{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>
