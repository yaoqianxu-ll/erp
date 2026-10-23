import axios from 'axios'
import { toast } from './toast'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('erp_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }
      toast.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    if (status === 401) {
      localStorage.removeItem('erp_token')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    } else if (status === 403) {
      toast.error('没有操作权限，请联系管理员')
    } else {
      toast.error(message)
    }
    return Promise.reject(error)
  },
)

export const api = {
  get<T>(url: string, params?: Record<string, unknown>) {
    return http.get(url, { params }) as unknown as Promise<T>
  },
  post<T>(url: string, data?: unknown) {
    return http.post(url, data) as unknown as Promise<T>
  },
  put<T>(url: string, data?: unknown) {
    return http.put(url, data) as unknown as Promise<T>
  },
  patch<T>(url: string, data?: unknown) {
    return http.patch(url, data) as unknown as Promise<T>
  },
  delete<T>(url: string) {
    return http.delete(url) as unknown as Promise<T>
  },
}

export default http
