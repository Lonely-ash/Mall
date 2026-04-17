import { defineStore } from 'pinia'

const TOKEN_KEY = 'mall_token'
const USER_KEY = 'mall_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'admin'
  },
  actions: {
    setLogin(payload) {
      this.token = payload.token
      this.user = {
        userId: payload.userId,
        username: payload.username,
        balance: payload.balance,
        phone: payload.phone || '',
        status: payload.status,
        role: payload.role || (payload.username === 'admin' ? 'admin' : 'buyer')
      }
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    setUserProfile(payload) {
      if (!this.user) this.user = {}
      this.user = {
        ...this.user,
        ...payload
      }
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    updateBalance(newBalance) {
      if (!this.user) return
      this.user.balance = newBalance
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
