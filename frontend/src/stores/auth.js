import { defineStore } from 'pinia'
import authApi from '../api/authApi'

/** Central session state. cart.js and wishlist.js both check auth.isLoggedIn before calling their
 * APIs, and router/index.js reads this store's role to guard /admin/* routes - this is the piece
 * every other module depends on, mirroring how every backend module depends on auth.User. */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    role: localStorage.getItem('role') || null,
    userId: localStorage.getItem('userId') || null,
    fullName: localStorage.getItem('fullName') || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN',
  },
  actions: {
    // UC1.1.1
    async register(payload) {
      const res = await authApi.register(payload)
      this.setSession(res.data)
    },
    // UC1.1.2
    async login(payload) {
      const res = await authApi.login(payload)
      this.setSession(res.data)
    },
    // UC1.1.3
    async logout() {
      try { await authApi.logout() } catch (e) { /* token already invalid, ignore */ }
      this.token = this.role = this.userId = null
      this.fullName = ''
      localStorage.clear()
    },
    setSession(data) {
      this.token = data.token
      this.role = data.role
      this.userId = data.userId
      this.fullName = data.fullName
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('fullName', data.fullName)
    },
  },
})
