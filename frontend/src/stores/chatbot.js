import { defineStore } from 'pinia'
import chatbotApi from '../api/chatbotApi'

/** Epic 14.1 state, used only by components/ChatbotWidget.vue but kept as a store (rather than
 * local component state) so the conversation survives navigating between pages. */
export const useChatbotStore = defineStore('chatbot', {
  state: () => ({ sessionId: null, messages: [], sending: false, handedOff: false }),
  actions: {
    async ensureSession() { // UC14.1.1
      if (!this.sessionId) {
        const res = await chatbotApi.start()
        this.sessionId = res.data
      }
    },
    async send(content) { // UC14.1.2 / UC14.1.3 / UC14.1.4
      await this.ensureSession()
      this.messages.push({ sender: 'USER', content })
      this.sending = true
      try {
        const res = await chatbotApi.send(this.sessionId, content)
        this.messages.push(res.data)
      } finally {
        this.sending = false
      }
    },
    async requestHandoff() { // UC14.1.5
      await this.ensureSession()
      await chatbotApi.handoff(this.sessionId)
      this.handedOff = true
    },
    async rate(messageId, rating) { // UC14.1.6
      await chatbotApi.rate(messageId, rating)
      const msg = this.messages.find((m) => m.id === messageId)
      if (msg) msg.rating = rating
    },
  },
})
