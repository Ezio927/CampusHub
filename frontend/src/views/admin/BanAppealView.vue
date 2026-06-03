<template>
  <div class="page">
    <a href="#" class="back" @click.prevent="$router.back()">< 返回</a>
    <h1 class="page-title">封禁申诉</h1>
    <div v-if="list.length===0" class="empty">暂无待处理的封禁申诉</div>
    <div v-for="item in list" :key="item.id" class="card">
      <div class="flex-between mb-sm">
        <strong>申诉 #{{ item.id }}</strong>
        <span class="badge" style="background:#f59e0b">{{ item.status }}</span>
      </div>
      <p class="text-sm text-muted">用户ID：{{ item.userId }} · {{ item.createdAt?.substring(0,16)?.replace('T',' ') }}</p>
      <p v-if="item.reason" class="mt-sm">申诉理由：{{ item.reason }}</p>
      <div class="flex gap-sm mt-md">
        <button class="btn-primary btn-sm" @click="review(item.id, true)">通过并解封</button>
        <button class="btn-outline btn-sm" @click="review(item.id, false)">驳回</button>
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
  try {
    const r = await adminAPI.banAppeals()
    list.value = r.data || []
  } catch {
    list.value = []
  }
}

async function review(id, approved) {
  const ok = approved
    ? await modal.confirm('确认通过', '通过后将自动解除该用户的封禁状态。')
    : await modal.confirm('确认驳回', '驳回后该用户仍保持封禁状态。')
  if (!ok) return
  try {
    await adminAPI.reviewBanAppeal(id, approved)
    await load()
  } catch (e) {
    modal.alert('操作失败', e.message)
  }
}

onMounted(load)
</script>
