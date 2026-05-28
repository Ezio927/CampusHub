<template>
  <div class="page-container">
    <h2>用户评价</h2>
    <div v-if="reviews.length===0" class="empty-state">暂无评价</div>
    <div v-for="r in reviews" :key="r.id" class="card">
      <div class="flex-between"><span>评分: {{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span><small class="text-secondary">{{ r.createdAt }}</small></div>
      <p v-if="r.content" style="margin-top:4px">{{ r.content }}</p>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { reviewAPI } from '@/api/reviews'
const route = useRoute()
const reviews = ref([])
onMounted(async () => { const r = await reviewAPI.listByUser(route.params.userId); reviews.value = r.data || [] })
</script>
<style scoped>@import './ReviewsView.css';</style>
