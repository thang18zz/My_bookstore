import { defineStore } from 'pinia'
import cartApi from '../api/cartApi'

/** Epic 3.1/3.2 state, shown in the topbar badge on every page and consumed directly by
 * views/Checkout.vue when placing an order (Theme 4). */
export const useCartStore = defineStore('cart', {
  state: () => ({ items: [], total: 0 }),
  getters: {
    itemCount: (state) => state.items.reduce((sum, i) => sum + i.quantity, 0),
  },
  actions: {
    async fetch() { // UC3.1.1 / UC3.1.6
      const res = await cartApi.view()
      this.items = res.data.items
      this.total = res.data.total
    },
    async add(productId, quantity = 1) { // UC3.1.2
      const res = await cartApi.addItem({ productId, quantity })
      this.items = res.data.items
      this.total = res.data.total
    },
    async updateQuantity(productId, quantity) { // UC3.1.3
      const res = await cartApi.updateItem(productId, quantity)
      this.items = res.data.items
      this.total = res.data.total
    },
    async remove(productId) { // UC3.1.4
      const res = await cartApi.removeItem(productId)
      this.items = res.data.items
      this.total = res.data.total
    },
    async clear() { // UC3.1.5
      await cartApi.clear()
      this.items = []
      this.total = 0
    },
  },
})
