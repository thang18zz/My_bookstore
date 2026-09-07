<template>
  <div>
    <h2>Quản trị Chatbot</h2>

    <div class="stat-grid" v-if="stats"> <!-- UC14.2.4 -->
      <div class="stat-card"><h3>{{ stats.totalSessions }}</h3><p>Hội thoại</p></div>
      <div class="stat-card"><h3>{{ stats.handoffRatePercent.toFixed(1) }}%</h3><p>Tỉ lệ chuyển tiếp</p></div>
      <div class="stat-card"><h3>{{ stats.averageRating.toFixed(1) }}★</h3><p>Đánh giá TB</p></div>
    </div>

    <section>
      <h3>Hàng chờ chuyển tiếp</h3> <!-- UC14.2.3 -->
      <div v-for="s in handoffQueue" :key="s.id" class="session-row">
        <button @click="openSession(s.id)">Hội thoại #{{ s.id }}</button>
      </div>
    </section>

    <section v-if="activeSessionId">
      <h4>Hội thoại #{{ activeSessionId }}</h4>
      <div v-for="(m, i) in activeMessages" :key="i" class="msg">
        <strong>{{ m.sender }}:</strong> {{ m.content }}
      </div>
      <form @submit.prevent="reply">
        <input v-model="replyText" placeholder="Trả lời khách hàng..." />
        <button type="submit">Gửi</button>
        <button type="button" @click="closeSession">Đóng hội thoại</button>
      </form>
    </section>

    <section>
      <h3>Cấu hình FAQ / tri thức chatbot</h3> <!-- UC14.2.2 / UC14.2.5 -->
      <form @submit.prevent="saveFaq">
        <input v-model="faqForm.question" placeholder="Câu hỏi" required />
        <input v-model="faqForm.answer" placeholder="Câu trả lời" required />
        <input v-model="faqForm.keywords" placeholder="Từ khóa (phân cách bằng dấu phẩy)" />
        <button type="submit">{{ editingFaqId ? 'Cập nhật' : 'Thêm FAQ' }}</button>
      </form>
      <div v-for="f in faqs" :key="f.id" class="faq-row">
        <span><strong>{{ f.question }}</strong> - {{ f.answer }}</span>
        <button @click="editFaq(f)">Sửa</button>
        <button @click="deleteFaq(f.id)">Xóa</button>
      </div>
    </section>
  </div>
</template>

<script setup>
// Backend for this whole view: com.bookstore.chatbot.AdminChatbotController (UC14.2.1-5)
import { onMounted, reactive, ref } from 'vue'
import adminChatbotApi from '../../api/adminChatbotApi'

const stats = ref(null)
const handoffQueue = ref([])
const activeSessionId = ref(null)
const activeMessages = ref([])
const replyText = ref('')
const faqs = ref([])
const editingFaqId = ref(null)
const faqForm = reactive({ question: '', answer: '', keywords: '' })

onMounted(async () => {
  stats.value = (await adminChatbotApi.stats()).data
  handoffQueue.value = (await adminChatbotApi.handoffQueue({ page: 0, size: 20 })).data.content
  faqs.value = (await adminChatbotApi.listFaqs()).data
})

async function openSession(id) {
  activeSessionId.value = id
  activeMessages.value = (await adminChatbotApi.sessionMessages(id)).data // UC14.2.1
}

async function reply() {
  await adminChatbotApi.adminReply(activeSessionId.value, replyText.value) // UC14.2.3
  replyText.value = ''
  await openSession(activeSessionId.value)
}

async function closeSession() {
  await adminChatbotApi.close(activeSessionId.value)
  activeSessionId.value = null
  handoffQueue.value = (await adminChatbotApi.handoffQueue({ page: 0, size: 20 })).data.content
}

function editFaq(f) { editingFaqId.value = f.id; Object.assign(faqForm, f) }

async function saveFaq() {
  if (editingFaqId.value) await adminChatbotApi.updateFaq(editingFaqId.value, faqForm)
  else await adminChatbotApi.createFaq(faqForm)
  editingFaqId.value = null
  Object.assign(faqForm, { question: '', answer: '', keywords: '' })
  faqs.value = (await adminChatbotApi.listFaqs()).data
}

async function deleteFaq(id) {
  await adminChatbotApi.deleteFaq(id)
  faqs.value = (await adminChatbotApi.listFaqs()).data
}
</script>

<style scoped>
.stat-grid { display: flex; gap: 12px; margin-bottom: 16px; }
.stat-card { border: 1px solid #eee; border-radius: 8px; padding: 12px; text-align: center; flex: 1; }
.faq-row { display: flex; gap: 8px; align-items: center; padding: 6px 0; border-bottom: 1px solid #eee; }
</style>
