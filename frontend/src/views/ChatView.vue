<template>
  <div class="page">
    <div v-if="msgs.length===0" class="empty">💬<p class="mt-sm">暂无消息</p></div>
    <div v-for="m in msgs" :key="m.id" class="msg" :class="{mine:m.senderId===auth.user?.id}">
      <p><strong>{{ m.senderId===auth.user?.id?'我':'对方' }}</strong> <small class="text-muted">{{ m.createdAt?.substring(11,16) }}</small></p>
      <p>{{ m.content }}</p>
    </div>
    <div class="flex gap-sm mt-md"><input v-model="txt" placeholder="输入消息..." @keyup.enter="send" class="flex-1" /><button class="btn-primary btn-sm" @click="send">发送</button></div>
  </div>
</template>
<script setup>
import { ref,onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/api/chat'
const r=useRoute();const auth=useAuthStore();const msgs=ref([]);const txt=ref('')
async function send(){if(!txt.value.trim())return;await chatAPI.send(r.params.id,{content:txt.value});txt.value='';await load()}
async function load(){const res=await chatAPI.messages(r.params.id);msgs.value=res.data||[]}
onMounted(load)
</script>
<style scoped>
.msg{margin-bottom:8px;padding:10px 14px;border-radius:10px;background:#f1f5f9;max-width:80%}
.mine{background:#eef2ff;margin-left:auto}
</style>
