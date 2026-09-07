import { defineStore } from 'pinia'
import wishlistApi from '../api/wishlistApi'

/** Epic 2.3 state, checked from ProductDetail.vue to render the "yêu thích" heart as filled or not. */
export const useWishlistStore = defineStore('wishlist', {
  state: () => ({ items: [] }),
  getters: {
    isWishlisted: (state) => (productId) => state.items.some((i) => i.product.id === productId),
  },
  actions: {
    async fetch() { this.items = (await wishlistApi.list()).data }, // UC2.3.1
    async add(productId) { await wishlistApi.add(productId); await this.fetch() },   // UC2.3.2
    async remove(productId) { await wishlistApi.remove(productId); await this.fetch() }, // UC2.3.3
  },
})
