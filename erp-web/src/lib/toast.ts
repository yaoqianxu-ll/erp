import { reactive } from 'vue'

export type ToastType = 'success' | 'error' | 'info'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
}

let seed = 0
export const toastState = reactive<{ items: ToastItem[] }>({ items: [] })

function push(type: ToastType, message: string) {
  const id = ++seed
  toastState.items.push({ id, type, message })
  setTimeout(() => {
    const index = toastState.items.findIndex((item) => item.id === id)
    if (index > -1) toastState.items.splice(index, 1)
  }, 3200)
}

export const toast = {
  success: (message: string) => push('success', message),
  error: (message: string) => push('error', message),
  info: (message: string) => push('info', message),
}
