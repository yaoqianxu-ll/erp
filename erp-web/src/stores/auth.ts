import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/lib/http'
import type { LoginResult, MeResult, Menu, User } from '@/lib/types'

const TOKEN_KEY = 'erp_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<User | null>(null)
  const roles = ref<string[]>([])
  const menus = ref<Menu[]>([])

  const isAdmin = computed(() => roles.value.includes('admin'))
  const displayName = computed(() => user.value?.nickname || user.value?.username || '')

  async function login(username: string, password: string) {
    const result = await api.post<LoginResult>('/auth/login', { username, password })
    token.value = result.token
    localStorage.setItem(TOKEN_KEY, result.token)
    user.value = result.user
    roles.value = result.roles
    menus.value = result.menus
  }

  async function fetchMe() {
    const result = await api.get<MeResult>('/auth/me')
    user.value = result.user
    roles.value = result.roles
    menus.value = result.menus
  }

  function logout() {
    token.value = ''
    user.value = null
    roles.value = []
    menus.value = []
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, user, roles, menus, isAdmin, displayName, login, fetchMe, logout }
})
