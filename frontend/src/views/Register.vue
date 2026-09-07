<template>
  <div class="form-page">
    <h2>Đăng ký tài khoản</h2>
    <form @submit.prevent="submit">
      <input v-model="form.fullName" placeholder="Họ tên" required />
      <input v-model="form.email" type="email" placeholder="Email" required />
      <input v-model="form.password" type="password" placeholder="Mật khẩu" required />
      <input v-model="form.phone" placeholder="Số điện thoại" />
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit">Đăng ký</button>
    </form>
  </div>
</template>

<script setup>
// UC1.1.1 - Đăng ký tài khoản
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const form = reactive({ fullName: '', email: '', password: '', phone: '' })
const error = ref('')
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  try {
    await auth.register(form)
    router.push('/products')
  } catch (e) {
    error.value = e.message || 'Đăng ký thất bại'
  }
}
</script>
