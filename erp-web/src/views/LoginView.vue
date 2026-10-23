<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowRight,
  Boxes,
  Eye,
  EyeOff,
  LockKeyhole,
  ShieldCheck,
  TrendingUp,
  UserRound,
  Warehouse,
} from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Label from '@/components/ui/label/Label.vue'
import { useAuthStore } from '@/stores/auth'
import { toast } from '@/lib/toast'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const form = reactive({ username: 'admin', password: '123456' })
const loading = ref(false)
const showPassword = ref(false)

const demoAccounts = [
  { label: '管理员', username: 'admin', password: '123456', desc: '全部模块' },
  { label: '仓库管理员', username: 'warehouse', password: '123456', desc: '仅库存模块' },
]

function useDemo(account: (typeof demoAccounts)[number]) {
  form.username = account.username
  form.password = account.password
}

async function submit() {
  if (!form.username || !form.password) {
    toast.error('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    toast.success(`欢迎回来，${auth.displayName}`)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch {
    // 错误提示已由 http 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="grid min-h-screen lg:grid-cols-2">
    <div class="relative hidden overflow-hidden bg-slate-950 lg:flex lg:flex-col lg:justify-between">
      <div class="absolute -left-24 -top-24 h-96 w-96 rounded-full bg-blue-600/20 blur-3xl" />
      <div class="absolute -bottom-32 right-0 h-[28rem] w-[28rem] rounded-full bg-indigo-600/20 blur-3xl" />
      <div class="absolute inset-0 opacity-[0.07]" style="background-image: linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px); background-size: 48px 48px;" />

      <div class="relative z-10 flex items-center gap-3 p-12">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg shadow-blue-500/30">
          <Boxes class="h-5 w-5 text-white" />
        </div>
        <div>
          <p class="text-base font-semibold text-white">云帆 ERP</p>
          <p class="text-xs text-slate-400">进销存管理平台</p>
        </div>
      </div>

      <div class="relative z-10 px-12">
        <p class="mb-4 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs text-blue-200">
          <span class="h-1.5 w-1.5 rounded-full bg-blue-400" />
          采购 · 库存 · 销售 一体化
        </p>
        <h1 class="max-w-lg text-4xl font-semibold leading-tight tracking-tight text-white">
          让进销存管理<br />
          <span class="bg-gradient-to-r from-blue-400 to-indigo-300 bg-clip-text text-transparent">回归简单与高效</span>
        </h1>
        <p class="mt-4 max-w-md text-sm leading-relaxed text-slate-400">
          从商品档案、采购入库到销售出库，全链路数据实时联动，
          库存预警与业务单据一目了然。
        </p>

        <div class="mt-10 grid max-w-md grid-cols-3 gap-3">
          <div class="rounded-xl border border-white/10 bg-white/[0.04] p-4">
            <Warehouse class="h-5 w-5 text-blue-400" />
            <p class="mt-3 text-sm font-medium text-white">多仓管理</p>
            <p class="mt-1 text-[11px] text-slate-500">仓库与库存独立核算</p>
          </div>
          <div class="rounded-xl border border-white/10 bg-white/[0.04] p-4">
            <TrendingUp class="h-5 w-5 text-emerald-400" />
            <p class="mt-3 text-sm font-medium text-white">实时库存</p>
            <p class="mt-1 text-[11px] text-slate-500">出入库自动记流水</p>
          </div>
          <div class="rounded-xl border border-white/10 bg-white/[0.04] p-4">
            <ShieldCheck class="h-5 w-5 text-indigo-400" />
            <p class="mt-3 text-sm font-medium text-white">权限管控</p>
            <p class="mt-1 text-[11px] text-slate-500">角色菜单精细授权</p>
          </div>
        </div>
      </div>

      <div class="relative z-10 flex items-center justify-between px-12 pb-10 text-xs text-slate-500">
        <span>© 2026 云帆 ERP</span>
        <span>Spring Boot 3 · Vue 3</span>
      </div>
    </div>

    <div class="flex flex-col justify-center bg-slate-50 px-6 py-12 sm:px-12">
      <div class="mx-auto w-full max-w-md">
        <div class="mb-8 flex items-center gap-3 lg:hidden">
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600">
            <Boxes class="h-5 w-5 text-white" />
          </div>
          <div>
            <p class="text-base font-semibold text-slate-900">云帆 ERP</p>
            <p class="text-xs text-slate-400">进销存管理平台</p>
          </div>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-8 shadow-card">
          <h2 class="text-2xl font-semibold tracking-tight text-slate-900">欢迎回来</h2>
          <p class="mt-1.5 text-sm text-slate-500">登录后进入你的业务工作台</p>

          <form class="mt-7 space-y-5" @submit.prevent="submit">
            <div class="space-y-2">
              <Label for="username">登录账号</Label>
              <div class="relative">
                <UserRound class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input id="username" v-model="form.username" class="pl-9" placeholder="请输入账号" autocomplete="username" />
              </div>
            </div>

            <div class="space-y-2">
              <Label for="password">登录密码</Label>
              <div class="relative">
                <LockKeyhole class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input
                  id="password"
                  v-model="form.password"
                  class="pl-9 pr-10"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-slate-400 hover:text-slate-600"
                  @click="showPassword = !showPassword"
                >
                  <EyeOff v-if="showPassword" class="h-4 w-4" />
                  <Eye v-else class="h-4 w-4" />
                </button>
              </div>
            </div>

            <Button type="submit" class="h-10 w-full" :disabled="loading">
              {{ loading ? '登录中...' : '登 录' }}
              <ArrowRight v-if="!loading" class="h-4 w-4" />
            </Button>
          </form>

          <div class="mt-6 rounded-lg border border-slate-100 bg-slate-50 p-3.5">
            <p class="mb-2 text-[11px] font-medium text-slate-500">演示账号（点击填充）</p>
            <div class="flex gap-2">
              <button
                v-for="account in demoAccounts"
                :key="account.username"
                type="button"
                class="flex-1 rounded-md border border-slate-200 bg-white px-3 py-2 text-left transition hover:border-blue-300 hover:bg-blue-50/40"
                @click="useDemo(account)"
              >
                <span class="block text-xs font-medium text-slate-700">{{ account.label }}</span>
                <span class="block text-[10px] text-slate-400">{{ account.username }} / 123456</span>
              </button>
            </div>
          </div>
        </div>

        <p class="mt-6 text-center text-[11px] text-slate-400">
          登录即表示同意系统使用规范 · 数据仅用于演示
        </p>
      </div>
    </div>
  </div>
</template>
