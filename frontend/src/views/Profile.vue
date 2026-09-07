<template>
  <div>
    <h2>Thông tin cá nhân</h2>
    <form @submit.prevent="save">
      <p>Email: {{ profile.email }}</p>
      <input v-model="profile.fullName" placeholder="Họ tên" />
      <input v-model="profile.phone" placeholder="Số điện thoại" />
      <button type="submit">Lưu</button>
      <span v-if="saved">Đã lưu!</span>
    </form>
  </div>
</template>

<script setup>
// UC1.2.1 (load) + UC1.2.2 (update)
import { onMounted, reactive, ref } from 'vue'
import authApi from '../api/authApi'

const profile = reactive({ email: '', fullName: '', phone: '' })
const saved = ref(false)

onMounted(async () => {
  const res = await authApi.me()
  Object.assign(profile, res.data)
})

async function save() {
  const res = await authApi.updateMe({ fullName: profile.fullName, phone: profile.phone })
  Object.assign(profile, res.data)
  saved.value = true
}
</script>
