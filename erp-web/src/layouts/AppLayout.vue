<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { Boxes, ChevronRight, LogOut, Menu as MenuIcon, X } from 'lucide-vue-next'
import AppIcon from '@/components/AppIcon.vue'
import { useAuthStore } from '@/stores/auth'
import { cn } from '@/lib/utils'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const sidebarOpen = ref(false)

const rootMenus = computed(() => auth.menus)

const currentTitle = computed(() => (route.meta.title as string) || '仪表盘')

const roleLabel = computed(() => {
  if (auth.roles.includes('admin')) return '超级管理员'
  if (auth.roles.includes('warehouse')) return '仓库管理员'
  return '业务人员'
})

const initial = computed(() => (auth.displayName || 'U').slice(0, 1))

function isActive(path?: string) {
  return path ? route.path === path : false
}

function closeSidebar() {
  sidebarOpen.value = false
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-slate-50">
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-30 bg-slate-950/40 lg:hidden"
      @click="closeSidebar"
    />

    <aside
      :class="
        cn(
          'fixed inset-y-0 left-0 z-40 flex w-60 flex-col border-r border-slate-200 bg-white transition-transform duration-200 lg:static lg:translate-x-0',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        )
      "
    >
      <div class="flex h-16 items-center justify-between px-5">
        <div class="flex items-center gap-2.5">
          <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-md shadow-blue-500/20">
            <Boxes class="h-5 w-5 text-white" />
          </div>
          <div>
            <p class="text-[15px] font-semibold leading-tight text-slate-900">云帆 ERP</p>
            <p class="text-[10px] tracking-wide text-slate-400">进销存管理平台</p>
          </div>
        </div>
        <button class="rounded-md p-1 text-slate-400 hover:bg-slate-100 lg:hidden" @click="closeSidebar">
          <X class="h-4 w-4" />
        </button>
      </div>

      <nav class="flex-1 space-y-5 overflow-y-auto px-3 py-4 scrollbar-thin">
        <template v-for="menu in rootMenus" :key="menu.id">
          <div v-if="menu.children && menu.children.length">
            <p class="mb-1.5 flex items-center gap-2 px-3 text-[11px] font-medium uppercase tracking-wider text-slate-400">
              <AppIcon :name="menu.icon" class="h-3.5 w-3.5" />
              {{ menu.name }}
            </p>
            <div class="space-y-1">
              <RouterLink
                v-for="child in menu.children"
                :key="child.id"
                :to="child.path || '/'"
                :class="
                  cn(
                    'group flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive(child.path)
                      ? 'bg-blue-50 text-blue-700'
                      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                  )
                "
                @click="closeSidebar"
              >
                <AppIcon
                  :name="child.icon"
                  :class="
                    cn(
                      'h-4 w-4 shrink-0',
                      isActive(child.path) ? 'text-blue-600' : 'text-slate-400 group-hover:text-slate-600',
                    )
                  "
                />
                {{ child.name }}
              </RouterLink>
            </div>
          </div>
          <RouterLink
            v-else-if="menu.path"
            :to="menu.path"
            :class="
              cn(
                'flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                isActive(menu.path)
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
              )
            "
            @click="closeSidebar"
          >
            <AppIcon
              :name="menu.icon"
              :class="cn('h-4 w-4', isActive(menu.path) ? 'text-blue-600' : 'text-slate-400')"
            />
            {{ menu.name }}
          </RouterLink>
        </template>
        <div v-if="rootMenus.length === 0" class="px-3 py-6 text-center text-xs text-slate-400">
          暂未分配菜单权限
        </div>
      </nav>

      <div class="border-t border-slate-100 p-3">
        <div class="flex items-center gap-2 rounded-lg bg-slate-50 px-3 py-2.5">
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-700">
            {{ initial }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-xs font-medium text-slate-800">{{ auth.displayName }}</p>
            <p class="truncate text-[10px] text-slate-400">{{ roleLabel }}</p>
          </div>
          <button class="rounded-md p-1.5 text-slate-400 transition hover:bg-white hover:text-red-500" title="退出登录" @click="logout">
            <LogOut class="h-4 w-4" />
          </button>
        </div>
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex h-16 shrink-0 items-center gap-3 border-b border-slate-200 bg-white/90 px-4 backdrop-blur lg:px-6">
        <button class="rounded-lg p-2 text-slate-500 hover:bg-slate-100 lg:hidden" @click="sidebarOpen = true">
          <MenuIcon class="h-5 w-5" />
        </button>
        <div class="flex items-center gap-1.5 text-sm">
          <span class="text-slate-400">首页</span>
          <ChevronRight class="h-3.5 w-3.5 text-slate-300" />
          <span class="font-medium text-slate-700">{{ currentTitle }}</span>
        </div>
        <div class="ml-auto flex items-center gap-3">
          <span class="hidden items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-medium text-emerald-600 sm:flex">
            <span class="h-1.5 w-1.5 rounded-full bg-emerald-500" />
            服务正常
          </span>
          <div class="hidden h-5 w-px bg-slate-200 sm:block" />
          <div class="flex items-center gap-2">
            <div class="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-xs font-semibold text-white">
              {{ initial }}
            </div>
            <div class="hidden leading-tight sm:block">
              <p class="text-xs font-medium text-slate-800">{{ auth.displayName }}</p>
              <p class="text-[10px] text-slate-400">{{ auth.user?.username }}</p>
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-4 scrollbar-thin lg:p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
