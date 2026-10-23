import { reactive } from 'vue'

interface ConfirmState {
  open: boolean
  title: string
  message: string
  confirmText: string
  resolve?: (value: boolean) => void
}

export const confirmState = reactive<ConfirmState>({
  open: false,
  title: '',
  message: '',
  confirmText: '确认删除',
})

export function confirmAction(title: string, message: string, confirmText = '确认删除') {
  return new Promise<boolean>((resolve) => {
    confirmState.title = title
    confirmState.message = message
    confirmState.confirmText = confirmText
    confirmState.resolve = resolve
    confirmState.open = true
  })
}

export function answerConfirm(value: boolean) {
  confirmState.resolve?.(value)
  confirmState.open = false
  confirmState.resolve = undefined
}
