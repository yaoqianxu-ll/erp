export function money(value?: number | string | null) {
  const num = Number(value ?? 0)
  return `¥${num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function number(value?: number | string | null) {
  return Number(value ?? 0).toLocaleString('zh-CN')
}
