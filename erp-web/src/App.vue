<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toaster from '@/components/Toaster.vue'
import ConfirmHost from '@/components/ConfirmHost.vue'
</script>

<template>
  <RouterView />
  <Toaster />
  <ConfirmHost />
</template>
