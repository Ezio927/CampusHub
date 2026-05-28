<template>
  <div class="page-container">
    <h2>聊天</h2>
    <div v-if="messages.length===0" class="empty-state">暂无消息</div>
    <div v-for="m in messages" :key="m.id" class="card" :style="{ background: m.senderId === auth.currentUser?.id ? '#e0e7ff' : '#fff' }">
      <p><strong>{{ m.senderId === auth.currentUser?.id ? '我' : '对方' }}</strong> <small>{{ m.createdAt }}</small></p>
      <p>{{ m.content }}</p>
    </div>
    <div class="flex-row gap-sm" style="margin-top:12px">
      <input v-model="text" placeholder="输入消息..." @keyup.enter="send" style="flex:1" />
      <button class="btn-primary btn-sm" @click="send">发送</button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/api/conversations'
const route = useRoute(); const auth = useAuthStore()
const messages = ref([]); const text = ref('')
async function send() { if (!text.value.trim()) return; await chatAPI.sendMessage(route.params.id, { content: text.value }); text.value = ''; await load() }
async function load() { const r = await chatAPI.getMessages(route.params.id); messages.value = r.data || [] }
onMounted(load)
</script>
