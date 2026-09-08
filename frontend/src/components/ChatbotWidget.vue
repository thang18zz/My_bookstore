<template>
  <div class="chat-widget">
    <button class="chat-fab" @click="open = !open">💬</button>

    <div v-if="open" class="chat-window">
      <div class="chat-header">
        Trợ lý ảo Book Store
        <button v-if="!chatbot.handedOff" @click="chatbot.requestHandoff()">Gặp nhân viên</button> <!-- UC14.1.5 -->
      </div>

      <div class="chat-body" ref="body">
        <div v-for="(m, idx) in chatbot.messages" :key="idx" :class="['msg', m.sender.toLowerCase()]">
          <p>{{ m.content }}</p>
          <div v-if="m.sender === 'BOT' && m.id" class="rate-row"> <!-- UC14.1.6 -->
            <button v-for="n in [1,2,3,4,5]" :key="n"
                    :class="{ active: m.rating >= n }"
                    @click="chatbot.rate(m.id, n)">★</button>
          </div>
        </div>
        <p v-if="chatbot.sending" class="typing">Đang trả lời...</p>
        <p v-if="chatbot.handedOff" class="handoff-note">Đã chuyển cho nhân viên hỗ trợ.</p>
      </div>

      <form class="chat-input" @submit.prevent="send">
        <input v-model="draft" placeholder="Hỏi về sách, đơn hàng, gợi ý..." :disabled="chatbot.handedOff" />
        <button type="submit" :disabled="chatbot.handedOff || !draft">Gửi</button>
      </form>
    </div>
  </div>
</template>

<script setup>
/**
 * Frontend half of Epic 14.1. Every message the user types goes through chatbot.send(), which:
 *  - opens a ChatSession on first use (UC14.1.1)
 *  - is answered by the backend's Groq-powered pipeline, covering product Q&A (UC14.1.2),
 *    order lookup (UC14.1.3) and recommendations (UC14.1.4) transparently from this component's
 *    point of view - the intent routing all happens server-side.
 * "Gặp nhân viên" and the star rating map directly to UC14.1.5 and UC14.1.6.
 */
import { nextTick, ref, watch } from 'vue'
import { useChatbotStore } from '../stores/chatbot'

const chatbot = useChatbotStore()
const open = ref(false)
const draft = ref('')
const body = ref(null)

watch(() => chatbot.messages.length, () => {
  nextTick(() => { if (body.value) body.value.scrollTop = body.value.scrollHeight })
})

async function send() {
  const text = draft.value
  draft.value = ''
  await chatbot.send(text)
}
</script>

<style scoped>
.chat-widget { position: fixed; bottom: 20px; right: 20px; z-index: 1000; }
.chat-fab { width: 56px; height: 56px; border-radius: 50%; font-size: 1.4rem; border: none;
            background: #2563eb; color: white; cursor: pointer; }
.chat-window { position: absolute; bottom: 68px; right: 0; width: 320px; height: 420px;
               background: white; border: 1px solid #ddd; border-radius: 8px; display: flex;
               flex-direction: column; box-shadow: 0 4px 16px rgba(0,0,0,0.15); }
.chat-header { padding: 8px 12px; background: #1f2937; color: white; display: flex;
               justify-content: space-between; align-items: center; font-size: 0.9rem; }
.chat-header button { font-size: 0.75rem; }
.chat-body { flex: 1; overflow-y: auto; padding: 8px; }
.msg { margin-bottom: 8px; }
.msg.user p { background: #2563eb; color: white; padding: 6px 10px; border-radius: 8px; margin-left: auto; width: fit-content; }
.msg.bot p, .msg.admin p { background: #f3f4f6; padding: 6px 10px; border-radius: 8px; width: fit-content; }
.rate-row button { border: none; background: none; cursor: pointer; color: #d1d5db; }
.rate-row button.active { color: #f59e0b; }
.typing, .handoff-note { font-size: 0.8rem; color: #6b7280; }
.chat-input { display: flex; border-top: 1px solid #eee; }
.chat-input input { flex: 1; border: none; padding: 8px; }
.chat-input button { border: none; background: #2563eb; color: white; padding: 0 12px; }
</style>
