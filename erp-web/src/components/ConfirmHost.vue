<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import Button from '@/components/ui/button/Button.vue'
import { answerConfirm, confirmState } from '@/lib/confirm'

function onOpenChange(open: boolean) {
  if (!open) answerConfirm(false)
}
</script>

<template>
  <Dialog :open="confirmState.open" @update:open="onOpenChange">
    <DialogContent class="max-w-md">
      <DialogHeader>
        <div class="mb-1 flex h-10 w-10 items-center justify-center rounded-full bg-red-50">
          <AlertTriangle class="h-5 w-5 text-red-500" />
        </div>
        <DialogTitle>{{ confirmState.title }}</DialogTitle>
        <DialogDescription>{{ confirmState.message }}</DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button variant="outline" @click="answerConfirm(false)">取消</Button>
        <Button variant="destructive" @click="answerConfirm(true)">{{ confirmState.confirmText }}</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
