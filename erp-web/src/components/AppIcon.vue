<script setup lang="ts">
import {
  ArrowLeftRight,
  Boxes,
  LayoutDashboard,
  ListTree,
  Package,
  Settings,
  ShieldCheck,
  ShoppingCart,
  Tags,
  Truck,
  Users,
  Warehouse,
  type LucideIcon,
} from 'lucide-vue-next'
import { computed } from 'vue'

const props = defineProps<{ name?: string; class?: string }>()

const icons: Record<string, LucideIcon> = {
  LayoutDashboard,
  Settings,
  Users,
  ShieldCheck,
  ListTree,
  Package,
  Tags,
  Boxes,
  ArrowLeftRight,
  Warehouse,
  ShoppingCart,
  Truck,
}

const icon = computed(() => icons[props.name || ''] || Package)
</script>

<template>
  <component :is="icon" :class="props.class || 'h-4 w-4'" />
</template>
