<template>
  <div class="page">
    <div class="flex-between mb-md">
      <h1 class="page-title" style="margin:0">需求大厅</h1>
      <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="btn-primary btn-sm">+ 发布需求</router-link>
    </div>
    <div class="flex gap-sm mb-md flex-wrap">
      <select v-model="store.filters.category" @change="store.fetch()" class="sel">
        <option value="">全部分类</option>
        <option value="ERRAND">跑腿代办</option>
        <option value="SKILL">技能求助</option>
        <option value="STUDY">学习互助</option>
        <option value="SECONDHAND">二手交易</option>
        <option value="OTHER">其他</option>
      </select>
      <input v-model="store.filters.keyword" placeholder="搜索关键词..." @input="debounce" class="kw" />
      <label class="open-only"><input type="checkbox" v-model="store.filters.openOnly" @change="store.fetch()" /> 仅看可接单</label>
    </div>
    <div v-if="store.list.length===0" class="empty">📭<p class="mt-sm">暂无需求</p></div>
    <div v-for="d in store.list" :key="d.id" class="dc" @click="goDetail(d.id)">
      <div class="dt"><span class="dtl">{{ d.title }}</span><span class="cat" :style="{background:catBg(d.category)}">{{ catTxt(d.category) }}</span></div>
      <p class="dd">{{ d.description }}</p>
      <div class="df">
        <span class="text-muted text-sm">{{ d.locationTag || '不限位置' }} · {{ d.publisher?.username }}</span>
        <span class="dp">{{ d.rewardType === 'MONEY' ? '¥' + d.rewardAmount : d.rewardAmount + '积分' }}</span>
      </div>
      <div v-if="d.urgent" class="mt-sm"><span class="du">🔥 紧急</span></div>
    </div>
  </div>
</template>
<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandStore } from '@/stores/demands'
import { useAuthStore } from '@/stores/auth'
const store = useDemandStore()
const auth = useAuthStore()
const router = useRouter()
let t
function debounce() { clearTimeout(t); t = setTimeout(() => store.fetch(), 300) }
function catTxt(c) { return { ERRAND:'跑腿代办', SKILL:'技能求助', STUDY:'学习互助', SECONDHAND:'二手交易', OTHER:'其他' }[c] || c }
function catBg(c) { return { ERRAND:'#f59e0b', SKILL:'#6366f1', STUDY:'#10b981', SECONDHAND:'#ef4444', OTHER:'#8b5cf6' }[c] || '#888' }
function goDetail(id) { router.push('/demands/' + id) }
onMounted(() => store.fetch())
</script>
<style scoped>
.sel{width:auto;min-width:100px;border-radius:10px;padding:10px 14px;font-weight:500}
.kw{flex:1;min-width:140px;border-radius:10px;padding:10px 14px}
.open-only{display:inline-flex;align-items:center;gap:4px;font-size:13px;color:var(--text-secondary);cursor:pointer;white-space:nowrap}
.open-only input{width:auto}
.dc{background:#fff;border-radius:14px;padding:20px;margin-bottom:12px;border:1px solid var(--border);cursor:pointer;transition:all .2s}
.dc:hover{box-shadow:0 4px 20px rgba(0,0,0,.08);transform:translateY(-2px)}
.dt{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:10px}
.dtl{font-size:17px;font-weight:700;flex:1;margin-right:10px}
.cat{display:inline-flex;padding:4px 10px;border-radius:20px;font-size:11px;font-weight:700;color:#fff;white-space:nowrap}
.dd{font-size:14px;color:var(--text-secondary);margin-bottom:12px;line-height:1.6;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.df{display:flex;justify-content:space-between;align-items:center}
.dp{font-size:18px;font-weight:800;color:var(--danger)}
.du{display:inline-flex;align-items:center;gap:3px;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:700;color:#fff;background:linear-gradient(135deg,#f97316,#ef4444)}
</style>
