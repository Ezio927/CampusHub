<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">信用分申诉</h1>
    <div v-if="list.length===0" class="empty">暂无待处理申诉</div>
    <div v-for="a in list" :key="a.id" class="card">
      <div class="flex-between mb-sm">
        <strong>申诉 #{{ a.id }}</strong>
        <span class="badge" style="background:#f59e0b">{{ a.status }}</span>
      </div>
      <p class="text-sm text-muted">用户ID: {{ a.userId }} · {{ a.createdAt?.substring(0,16)?.replace('T',' ') }}</p>
      <p v-if="a.reason" class="mt-sm">理由：{{ a.reason }}</p>
      <div class="flex gap-sm mt-md">
        <button class="btn-primary btn-sm" @click="review(a.id,true)">通过（恢复至60分）</button>
        <button class="btn-outline btn-sm" @click="review(a.id,false)">驳回</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api/admin'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const list = ref([])
async function load() {
  try { const r = await adminAPI.creditAppeals(); list.value = r.data || [] } catch { list.value = [] }
}
async function review(id, approved) {
  const ok = approved ? await modal.confirm('确认通过', '将用户信用分恢复至60分') : await modal.confirm('确认驳回', '驳回该申诉')
  if (!ok) return
  try { await adminAPI.reviewCreditAppeal(id, approved); await load() } catch (e) { modal.alert('操作失败', e.message) }
}
onMounted(load)
</script>
