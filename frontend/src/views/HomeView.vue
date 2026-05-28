<template>
  <div>
    <div class="flex-between home-header">
      <h2 class="page-title" style="margin:0">需求大厅</h2>
      <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="btn-primary btn-sm">+ 发布需求</router-link>
    </div>
    <div class="flex gap-sm mb-md flex-wrap">
      <select v-model="store.filters.category" @change="store.fetchDemands()" class="category-select">
        <option value="">全部分类</option>
        <option value="ERRAND">跑腿</option><option value="SKILL">技能</option>
        <option value="STUDY">学习</option><option value="SECONDHAND">二手</option>
      </select>
      <input v-model="store.filters.keyword" placeholder="搜索关键词" @input="debounceSearch" class="search-input" />
    </div>

    <div v-if="store.demands.length === 0" class="empty-state"><span class="icon">📭</span><p>暂无需求</p></div>
    <div v-for="d in store.demands" :key="d.id" class="card card-clickable demand-card" @click="$router.push(`/demands/${d.id}`)">
      <div class="flex-between">
        <span class="demand-title">{{ d.title }}</span>
        <span class="badge" :style="{ background: catColor(d.category) }">{{ catText(d.category) }}</span>
      </div>
      <p class="demand-desc text-secondary">{{ d.description?.substring(0, 80) }}{{ d.description?.length > 80 ? '...' : '' }}</p>
      <div class="flex-between demand-meta">
        <span class="text-muted">{{ d.locationTag || '不限位置' }}</span>
        <span class="text-bold" style="color:var(--danger)">{{ d.rewardType === 'MONEY' ? '¥' + d.rewardAmount : d.rewardAmount + '积分' }}</span>
      </div>
      <div class="flex-between mt-sm demand-meta">
        <small class="text-muted">{{ d.publisher?.username }} · {{ formatTime(d.createdAt) }}</small>
        <span v-if="d.urgent" class="badge" style="background:var(--danger)">🔥 紧急</span>
      </div>
    </div>
  </div>
</template>
<script setup>
import { onMounted } from 'vue'
import { useDemandStore } from '@/stores/demands'
import { useAuthStore } from '@/stores/auth'
const store = useDemandStore()
const auth = useAuthStore()
let timer = null
function debounceSearch() { clearTimeout(timer); timer = setTimeout(() => store.fetchDemands(), 300) }
function catText(c) { return { ERRAND:'跑腿', SKILL:'技能', STUDY:'学习', SECONDHAND:'二手' }[c] || c }
function catColor(c) { return { ERRAND:'#f59e0b', SKILL:'#4f46e5', STUDY:'#22c55e', SECONDHAND:'#ef4444' }[c] || '#888' }
function formatTime(t) { if (!t) return ''; return new Date(t).toLocaleDateString() }
onMounted(() => store.fetchDemands())
</script>
<style scoped>@import './HomeView.css';</style>
