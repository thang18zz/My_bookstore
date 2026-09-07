import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/login', component: () => import('../views/Login.vue') },       // UC1.1.2
  { path: '/register', component: () => import('../views/Register.vue') }, // UC1.1.1
  { path: '/profile', component: () => import('../views/Profile.vue'), meta: { auth: true } }, // UC1.2.1/1.2.2
  { path: '/products', component: () => import('../views/ProductList.vue') },       // UC2.1.1/2.1.2/2.2.x
  { path: '/products/:id', component: () => import('../views/ProductDetail.vue') }, // UC2.1.3-5, review, wishlist
  { path: '/wishlist', component: () => import('../views/Wishlist.vue'), meta: { auth: true } }, // Epic 2.3
  { path: '/cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },          // Epic 3
  { path: '/checkout', component: () => import('../views/Checkout.vue'), meta: { auth: true } },  // Theme 4
  { path: '/orders', component: () => import('../views/OrderHistory.vue'), meta: { auth: true } },// UC5.1.1
  { path: '/orders/:id', component: () => import('../views/OrderDetail.vue'), meta: { auth: true } }, // UC5.1.2-4
  { path: '/addresses', component: () => import('../views/Addresses.vue'), meta: { auth: true } },// Epic 6.1
  { path: '/contact', component: () => import('../views/Contact.vue') },                          // Epic 13.1

  // ---- Admin (Themes 8-12, 14.2) ----
  { path: '/admin', component: () => import('../views/admin/Dashboard.vue'), meta: { admin: true } },
  { path: '/admin/products', component: () => import('../views/admin/Products.vue'), meta: { admin: true } },
  { path: '/admin/categories', component: () => import('../views/admin/Categories.vue'), meta: { admin: true } },
  { path: '/admin/orders', component: () => import('../views/admin/Orders.vue'), meta: { admin: true } },
  { path: '/admin/customers', component: () => import('../views/admin/Customers.vue'), meta: { admin: true } },
  { path: '/admin/inventory', component: () => import('../views/admin/Inventory.vue'), meta: { admin: true } },
  { path: '/admin/reviews', component: () => import('../views/admin/Reviews.vue'), meta: { admin: true } },
  { path: '/admin/chatbot', component: () => import('../views/admin/ChatbotAdmin.vue'), meta: { admin: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

// Route guard reused by every protected view instead of repeating a check in each component.
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLoggedIn) return '/login'
  if (to.meta.admin && !auth.isAdmin) return '/login'
  return true
})

export default router
