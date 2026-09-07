<template>
  <div>
    <h2>Liên hệ</h2>
    <div v-if="info"> <!-- UC13.1.1 -->
      <p>Email: {{ info.email }}</p>
      <p>Điện thoại: {{ info.phone }}</p>
      <p>Địa chỉ: {{ info.address }}</p>
    </div>

    <form @submit.prevent="submit"> <!-- UC13.1.2 -->
      <input v-model="form.name" placeholder="Họ tên" required />
      <input v-model="form.email" type="email" placeholder="Email" required />
      <textarea v-model="form.message" placeholder="Nội dung" required></textarea>
      <button type="submit">Gửi yêu cầu</button>
      <p v-if="sent">Đã gửi yêu cầu, chúng tôi sẽ phản hồi sớm!</p>
    </form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import contactApi from '../api/contactApi'

const info = ref(null)
const form = reactive({ name: '', email: '', message: '' })
const sent = ref(false)

onMounted(async () => { info.value = (await contactApi.info()).data })

async function submit() {
  await contactApi.submit(form)
  sent.value = true
  Object.assign(form, { name: '', email: '', message: '' })
}
</script>
