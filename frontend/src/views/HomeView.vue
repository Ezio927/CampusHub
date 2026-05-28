<template>
  <div>
    <div class="flex-between" style="margin-bottom:12px">
      <h2>需求大厅</h2>
      <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="btn-primary btn-sm">发布需求</router-link>
    </div>
    <div class="flex-row gap-sm" style="margin-bottom:12px;flex-wrap:wrap">
      <select v-model="store.filters.category" @change="store.fetchDemands()" style="width:auto">
        <option value="">全部分类</option>
        <option value="ERRAND">跑腿</option><option value="SKILL">技能</option>
        <option value="STUDY">学习</option><option value="SECONDHAND">二手</option>
      </select>
      <input v-model="store.filters.keyword" placeholder="搜索关键词" @input="debounceSearch" style="width:auto;flex:1;min-width:120px" />
    </div>

    <div v-if="store.demands.length === 0" class="empty-state">暂无需求</div>
    <div v-for="d in store.demands" :key="d.id" class="card" style="cursor:pointer" @click="$router.push(`/demands/${d.id}`)">
      <div class="flex-between">
        <h4>{{ d.title }}</h4>
        <span class="badge" :style="{ background: catColor(d.category) }">{{ catText(d.category) }}</span>
      </div>
      <p style="margin:8px 0" class="text-secondary">{{ d.description?.substring(0, 80) }}{{ d.description?.length > 80 ? '...' : '' }}</p>
      <div class="flex-between">
        <span class="text-secondary">{{ d.locationTag || '不限位置' }}</span>
        <span style="font-weight:bold;color:var(--danger)">{{ d.rewardType === 'MONEY' ? '¥' + d.rewardAmount : d.rewardAmount + '积分' }}</span>
      </div>
      <div style="margin-top:8px" class="flex-between">
        <small class="text-secondary">{{ d.publisher?.username }} · {{ formatTime(d.createdAt) }}</small>
        <span v-if="d.urgent" class="badge" style="background:var(--danger)">紧急</span>
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
