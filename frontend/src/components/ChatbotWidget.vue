<template>
  <div class="chat-widget">
    <!-- Floating Action Button -->
    <button class="chat-fab" @click="open = !open" :title="open ? 'Đóng chatbot' : 'Mở Trợ lý ảo AI'">
      <span v-if="!open">💬</span>
      <span v-else>✕</span>
      <span class="pulse-ring" v-if="!open"></span>
    </button>

    <!-- Chat Window -->
    <div v-if="open" class="chat-window">
      <!-- Header -->
      <div class="chat-header">
        <div class="chat-header-info">
          <span class="status-indicator"></span>
          <div>
            <div class="chat-title">Trợ lý ảo BookStore</div>
            <div class="chat-subtitle">Hỗ trợ bởi Groq AI</div>
          </div>
        </div>
        <div class="chat-header-actions">
          <button
            v-if="!chatbot.handedOff"
            class="btn-handoff"
            @click="chatbot.requestHandoff()"
            title="Chuyển sang nhân viên tư vấn"
          >
            Gặp nhân viên
          </button>
        </div>
      </div>

      <!-- Body / Messages -->
      <div class="chat-body" ref="body">
        <!-- Welcome banner if no messages -->
        <div v-if="chatbot.messages.length === 0" class="welcome-box">
          <div class="welcome-icon">🤖</div>
          <h4>Xin chào! Tôi có thể giúp gì cho bạn?</h4>
          <p>Hỏi tôi về thông tin sách, gợi ý sách hay, chính sách giao hàng hoặc đổi trả.</p>
          <div class="suggestions">
            <button
              v-for="s in quickSuggestions"
              :key="s"
              class="suggestion-chip"
              @click="askQuick(s)"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div
          v-for="(m, idx) in chatbot.messages"
          :key="idx"
          :class="['msg-row', m.sender.toLowerCase()]"
        >
          <div class="avatar" v-if="m.sender !== 'USER'">🤖</div>
          <div class="msg-bubble">
            <p>{{ m.content }}</p>
            <div v-if="m.sender === 'BOT' && m.id" class="rate-row">
              <span class="rate-label">Đánh giá:</span>
              <button
                v-for="n in [1,2,3,4,5]"
                :key="n"
                :class="{ active: m.rating >= n }"
                @click="chatbot.rate(m.id, n)"
              >
                ★
              </button>
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="chatbot.sending" class="typing-indicator">
          <div class="avatar">🤖</div>
          <div class="dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>

        <!-- Handoff Note -->
        <div v-if="chatbot.handedOff" class="handoff-note">
          👨‍💼 Đã kết nối với nhân viên hỗ trợ. Bạn có thể tiếp tục nhắn tin bên dưới.
        </div>
      </div>

      <!-- Quick prompts if session is active -->
      <div v-if="chatbot.messages.length > 0 && !chatbot.sending && !chatbot.handedOff" class="quick-bar">
        <button
          v-for="s in quickSuggestions"
          :key="s"
          class="quick-pill"
          @click="askQuick(s)"
        >
          {{ s }}
        </button>
      </div>

      <!-- Input Bar -->
      <form class="chat-input" @submit.prevent="send">
        <input
          v-model="draft"
          placeholder="Hỏi về sách, vận chuyển, gợi ý..."
          :disabled="chatbot.sending"
          ref="inputEl"
        />
        <button
          type="submit"
          :disabled="chatbot.sending || !draft.trim()"
          class="btn-send"
        >
          ➤
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { useChatbotStore } from '../stores/chatbot'

const chatbot = useChatbotStore()
const open = ref(false)
const draft = ref('')
const body = ref(null)
const inputEl = ref(null)

const quickSuggestions = [
  'Gợi ý sách lập trình hay?',
  'Chính sách giao hàng thế nào?',
  'Phương thức thanh toán?',
]

watch(() => chatbot.messages.length, () => {
  nextTick(() => {
    if (body.value) body.value.scrollTop = body.value.scrollHeight
  })
})

watch(open, (isOpen) => {
  if (isOpen) {
    nextTick(() => {
      if (body.value) body.value.scrollTop = body.value.scrollHeight
      if (inputEl.value) inputEl.value.focus()
    })
  }
})

async function send() {
  const text = draft.value.trim()
  if (!text) return
  draft.value = ''
  await chatbot.send(text)
}

async function askQuick(text) {
  draft.value = text
  await send()
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.chat-fab {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  font-size: 1.5rem;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #0ea5e9);
  color: white;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.chat-fab:hover {
  transform: scale(1.08);
}

.pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  border: 2px solid #38bdf8;
  animation: pulse 2s infinite;
  pointer-events: none;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.4); opacity: 0; }
}

.chat-window {
  position: absolute;
  bottom: 74px;
  right: 0;
  width: 360px;
  height: 520px;
  background: #ffffff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.08);
  animation: slideUp 0.25s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  width: 10px;
  height: 10px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 8px #10b981;
}

.chat-title {
  font-weight: 700;
  font-size: 0.95rem;
}

.chat-subtitle {
  font-size: 0.75rem;
  color: #94a3b8;
}

.btn-handoff {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-handoff:hover {
  background: rgba(255, 255, 255, 0.25);
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-box {
  text-align: center;
  padding: 20px 10px;
  color: #334155;
}

.welcome-icon {
  font-size: 2.5rem;
  margin-bottom: 8px;
}

.welcome-box h4 {
  margin: 0 0 6px;
  font-size: 1rem;
}

.welcome-box p {
  font-size: 0.82rem;
  color: #64748b;
  margin: 0 0 16px;
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-chip {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 0.85rem;
  color: #4f46e5;
  text-align: left;
  cursor: pointer;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.2s;
}

.suggestion-chip:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateX(4px);
}

.msg-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.msg-row.user {
  justify-content: flex-end;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e0e7ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 0.9rem;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.25);
}

.msg-row.bot .msg-bubble,
.msg-row.admin .msg-bubble {
  background: #ffffff;
  color: #0f172a;
  border-bottom-left-radius: 4px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.msg-bubble p {
  margin: 0;
}

.rate-row {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.75rem;
  color: #94a3b8;
  border-top: 1px dashed #e2e8f0;
  padding-top: 4px;
}

.rate-row button {
  border: none;
  background: none;
  cursor: pointer;
  color: #cbd5e1;
  font-size: 0.95rem;
  padding: 0 1px;
}

.rate-row button.active {
  color: #f59e0b;
}

.typing-indicator {
  display: flex;
  gap: 8px;
  align-items: center;
}

.dots {
  display: flex;
  gap: 4px;
  background: white;
  padding: 10px 14px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.dots span {
  width: 6px;
  height: 6px;
  background: #94a3b8;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dots span:nth-child(1) { animation-delay: -0.32s; }
.dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.handoff-note {
  background: #fef3c7;
  color: #92400e;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  text-align: center;
}

.quick-bar {
  display: flex;
  gap: 6px;
  padding: 8px 12px;
  background: #f1f5f9;
  border-top: 1px solid #e2e8f0;
  overflow-x: auto;
}

.quick-pill {
  white-space: nowrap;
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  font-size: 0.75rem;
  padding: 4px 10px;
  cursor: pointer;
  color: #334155;
  transition: all 0.2s;
}

.quick-pill:hover {
  background: #4f46e5;
  color: white;
  border-color: #4f46e5;
}

.chat-input {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  background: white;
  border-top: 1px solid #e2e8f0;
  gap: 8px;
}

.chat-input input {
  flex: 1;
  border: 1px solid #e2e8f0;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.9rem;
  outline: none;
  font-family: inherit;
}

.chat-input input:focus {
  border-color: #4f46e5;
}

.btn-send {
  background: #4f46e5;
  color: white;
  border: none;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transition: background 0.2s;
}

.btn-send:hover:not(:disabled) {
  background: #4338ca;
}

.btn-send:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}
</style>
