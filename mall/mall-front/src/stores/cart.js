import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  getters: {
    totalCount: (state) => state.items.reduce((sum, i) => sum + (i.num || 0), 0)
  },
  actions: {
    setItems(items) {
      this.items = Array.isArray(items) ? items : []
    },
    clearAll() {
      this.items = []
    }
  }
})
