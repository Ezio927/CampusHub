<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">用户评价</h1>
    <div v-if="list.length===0" class="empty">暂无评价</div>
    <div v-for="r in list" :key="r.id" class="card">
      <div class="flex-between">
        <span class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span>
        <span class="text-sm text-muted">{{ r.createdAt?.substring(0,10) }}</span>
      </div>
      <p v-if="r.content" class="mt-sm">{{ r.content }}</p>
      <div v-if="r.orderId" class="text-xs text-muted mt-sm">订单 #{{ r.orderId }}</div>
      <button v-if="canAppeal(r)" class="btn-outline btn-sm mt-md" @click="submitReviewAppeal(r)">评价申诉</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { reviewAPI } from '@/api/reviews'
import { arbitrationAPI } from '@/api/arbitrations'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const auth = useAuthStore()
const modal = useModal()
const list = ref([])

function canAppeal(item) {
  return auth.user?.id && Number(route.params.userId) === auth.user.id && !!item.orderId
}

async function load() {
  try { const res = await reviewAPI.byUser(route.params.userId); list.value = res.data || [] } catch { list.value = [] }
}

async function submitReviewAppeal(item) {
  const reason = await modal.prompt('评价申诉', '请填写申诉理由（必填）', '请说明你对这条评价有异议的原因。')
  if (reason === false) return
  if (!reason || !reason.trim()) {
    await modal.alert('理由不能为空', '请填写申诉理由。')
    return
  }
  try {
    await arbitrationAPI.create({
      orderId: item.orderId,
      reviewId: item.id,
      reason: reason.trim(),
      evidenceUrls: []
    })
    modal.alert('提交成功', '评价申诉已提交，请等待管理员处理。')
  } catch (e) {
    modal.alert('提交失败', e.message)
  }
}

onMounted(async () => {
  await auth.fetchUser().catch(() => {})
  await load()
})
</script>

<style scoped>
.stars{font-size:18px;letter-spacing:2px;color:#f59e0b;font-weight:700}
</style>
