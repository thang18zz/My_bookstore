<template>
  <div class="form-page">
    <h2>Đăng nhập</h2>
    <form @submit.prevent="submit">
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="password" type="password" placeholder="Mật khẩu" required />
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit">Đăng nhập</button>
    </form>
    <router-link to="/register">Chưa có tài khoản? Đăng ký</router-link>
  </div>
</template>

<script setup>
// UC1.1.2 - Đăng nhập
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const error = ref('')
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push(auth.isAdmin ? '/admin' : '/products')
  } catch (e) {
    error.value = e.message || 'Đăng nhập thất bại'
  }
}
</script>
