<template>
  <div class="page">
    <div class="flex-between mb-md">
      <h1 class="page-title" style="margin:0">需求大厅</h1>
      <router-link v-if="auth.isLoggedIn" to="/demands/publish" class="btn-primary btn-sm">+ 发布需求</router-link>
    </div>
    <div v-if="!auth.isLoggedIn" class="login-banner">
      <span>登录后即可发布需求、接单赚钱</span>
      <router-link to="/login" class="btn-primary btn-sm">去登录</router-link>
    </div>
    <div class="filter-bar">
      <select v-model="store.filters.category" @change="store.fetch()" class="sel">
        <option value="">全部分类</option>
        <option value="ERRAND">跑腿代办</option>
        <option value="SKILL">技能求助</option>
        <option value="STUDY">学习互助</option>
        <option value="SECONDHAND">二手交易</option>
        <option value="OTHER">其他</option>
      </select>
      <select v-model="store.filters.sortBy" @change="store.fetch()" class="sel">
        <option value="createdAt">最新发布</option>
        <option value="recommended">推荐排序</option>
        <option value="rewardAmount">报酬高低</option>
      </select>
      <select v-model="store.filters.campus" @change="store.fetch()" class="sel">
        <option value="">全部校区</option>
        <option value="鼓楼校区">鼓楼校区</option>
        <option value="仙林校区">仙林校区</option>
        <option value="浦口校区">浦口校区</option>
        <option value="苏州校区">苏州校区</option>
      </select>
      <input v-model="store.filters.keyword" placeholder="搜索关键词..." @input="debounce" class="kw" />
      <label class="open-only" :class="{ active: store.filters.openOnly }"><input type="checkbox" v-model="store.filters.openOnly" @change="store.fetch()" /> 仅看可接单</label>
    </div>
    <div v-if="store.list.length===0" class="empty"><p class="mt-sm">暂无需求</p></div>
    <div v-else class="card-grid">
    <div v-for="d in store.list" :key="d.id" class="dc" @click="goDetail(d.id)">
      <div class="dt"><span class="dtl">{{ d.title }}</span><span class="cat" :style="{background:catBg(d.category)}">{{ catTxt(d.category) }}</span></div>
      <p class="dd">{{ d.description }}</p>
      <div class="df">
        <span class="text-muted text-sm">{{ d.campus || d.locationTag || '不限位置' }} · {{ d.publisher?.username }}</span>
        <span class="dp">{{ d.rewardType === 'MONEY' ? '¥' + d.rewardAmount : d.rewardAmount + '额度分' }}</span>
      </div>
      <div v-if="d.urgent" class="mt-sm"><span class="du">紧急</span></div>
    </div>
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
function catBg(c) { return { ERRAND:'#fef08a', SKILL:'#c7d2fe', STUDY:'#bbf7d0', SECONDHAND:'#fecdd3', OTHER:'#e9d5ff' }[c] || '#e5e7eb' }
function goDetail(id) { router.push('/demands/' + id) }
onMounted(() => store.fetch())
</script>
<style scoped>
.login-banner{display:flex;align-items:center;justify-content:space-between;padding:12px 16px;margin-bottom:14px;background:var(--primary-light);border-radius:10px;border:2px solid var(--primary);font-size:14px;color:var(--primary);font-weight:700}
.filter-bar{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:18px}
.sel{width:auto;min-width:100px;border:2px solid black;box-shadow:3px 3px 0 rgba(0,0,0,1)}
.kw{flex:1;min-width:140px;border:2px solid black;box-shadow:3px 3px 0 rgba(0,0,0,1)}
.open-only{display:inline-flex;align-items:center;gap:4px;font-size:13px;color:var(--text-secondary);cursor:pointer;white-space:nowrap;font-weight:600}
.open-only input{width:auto}
.card-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}
.dc{background:var(--card);border-radius:12px;padding:20px;border:2px solid black;box-shadow:4px 4px 0px rgba(0,0,0,1);cursor:pointer;transition:all 0.2s ease}
.dc:hover{transform:scale(1.02);box-shadow:6px 6px 0px rgba(0,0,0,1)}
.dt{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:10px}
.dtl{font-size:17px;font-weight:700;flex:1;margin-right:10px}
.cat{display:inline-flex;padding:4px 12px;border-radius:10px;font-size:12px;font-weight:700;color:#111;border:1.5px solid black;white-space:nowrap}
.dd{font-size:14px;color:var(--text-secondary);margin-bottom:12px;line-height:1.6;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.df{display:flex;justify-content:space-between;align-items:center}
.dp{font-size:18px;font-weight:800;color:var(--danger)}
.du{display:inline-flex;align-items:center;gap:3px;padding:3px 12px;border-radius:10px;font-size:12px;font-weight:700;color:#111;border:1.5px solid black;background:#fef08a}
</style>
