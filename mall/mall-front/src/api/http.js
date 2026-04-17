import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 12000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = auth.token
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const auth = useAuthStore()
    const status = error?.response?.status
    const msg = error?.response?.data?.msg || error?.message || '请求失败'

    if (status === 401) {
      auth.logout()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(new Error(msg))
  }
)

export default http
