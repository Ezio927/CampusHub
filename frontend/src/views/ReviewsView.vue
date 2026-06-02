<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">用户评价</h1>
    <div v-if="list.length===0" class="empty"><p class="mt-sm">暂无评价</p></div>
    <div v-for="r in list" :key="r.id" class="card">
      <div class="flex-between"><span class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span><span class="text-sm text-muted">{{ r.createdAt?.substring(0,10) }}</span></div>
      <p v-if="r.content" class="mt-sm">{{ r.content }}</p>
    </div>
  </div>
</template>
<script setup>
import { ref,onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { reviewAPI } from '@/api/reviews'
const r=useRoute();const list=ref([])
onMounted(async()=>{const res=await reviewAPI.byUser(r.params.userId);list.value=res.data||[]})
</script>
<style scoped>
.stars{font-size:18px;letter-spacing:2px;color:#f59e0b;font-weight:700}
</style>
