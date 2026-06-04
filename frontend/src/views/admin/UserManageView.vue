<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">用户管理</h1>
    <input v-model="keyword" placeholder="搜索用户名/姓名/学号" class="mb-md" @input="search" />

    <div v-if="list.length === 0" class="empty"><p class="mt-sm">暂无用户</p></div>
    <div v-for="user in list" :key="user.id" class="card user-card">
      <div class="user-main">
        <div class="text-bold">{{ user.username }}</div>
        <div class="text-sm text-muted">
          {{ user.realName || '未实名' }} · {{ user.studentId }} · {{ user.status }} · 信用 {{ user.creditScore }} · 额度
          {{ user.quotaAvailable || 0 }}/冻结{{ user.quotaFrozen || 0 }}
        </div>
        <div class="rating-row">
          <span>发布评分：{{ ratingText(user.publisherRating, user.publisherRatingCount) }}</span>
          <span>接单评分：{{ ratingText(user.acceptorRating, user.acceptorRatingCount) }}</span>
        </div>
      </div>
      <div class="actions">
        <button class="btn-outline btn-sm" @click="adjustRating(user, 'PUBLISHER')">调发布评分</button>
        <button class="btn-outline btn-sm" @click="adjustRating(user, 'ACCEPTOR')">调接单评分</button>
        <button class="btn-outline btn-sm" @click="adjustCredit(user.id)">调信用</button>
        <button class="btn-outline btn-sm" @click="adjustQuota(user.id)">调额度</button>
        <button v-if="user.status !== 'BANNED' && user.role !== 'ADMIN'" class="btn-danger btn-sm" @click="ban(user.id)">封禁</button>
        <button v-else-if="user.status === 'BANNED'" class="btn-outline btn-sm" @click="unban(user.id)">解封</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminAPI } from '@/api/admin'
import { ratingText } from '@/utils/userDisplay'
import { errorMessage } from '@/utils/errors'
import { useModal } from '@/composables/useModal'

const modal = useModal()
const list = ref([])
const keyword = ref('')

async function search() {
  const res = await adminAPI.users({ keyword: keyword.value })
  list.value = res.data || []
}

async function ban(id) {
  try {
    await adminAPI.ban(id)
    search()
  } catch (err) {
    modal.alert('操作失败', errorMessage(err))
  }
}

async function unban(id) {
  try {
    await adminAPI.unban(id)
    search()
  } catch (err) {
    modal.alert('操作失败', errorMessage(err))
  }
}

async function askAdjust(title) {
  const raw = await modal.prompt(title, '请输入调整值，正数增加，负数扣除', '例如：10 或 -5')
  if (raw === false) return null
  if (raw === null || raw === undefined || !String(raw).trim()) return null
  const amount = Number.parseInt(String(raw).trim(), 10)
  if (Number.isNaN(amount) || amount === 0) {
    await modal.alert('调整失败', '调整值必须是非零整数')
    return null
  }
  const reason = await modal.prompt(title, '请输入调整原因', '管理员手动调整')
  return { amount, reason: reason || '管理员手动调整' }
}

async function adjustCredit(id) {
  const body = await askAdjust('调整信用分')
  if (!body) return
  try {
    await adminAPI.adjustCredit(id, body)
    await search()
  } catch (err) {
    modal.alert('调整失败', errorMessage(err))
  }
}

async function adjustQuota(id) {
  const body = await askAdjust('调整额度分')
  if (!body) return
  try {
    await adminAPI.adjustQuota(id, body)
    await search()
  } catch (err) {
    modal.alert('调整失败', errorMessage(err))
  }
}

async function adjustRating(user, role) {
  const label = role === 'PUBLISHER' ? '发布者评分' : '接单者评分'
  const currentAverage = role === 'PUBLISHER' ? user.publisherRating : user.acceptorRating
  const currentCount = role === 'PUBLISHER' ? user.publisherRatingCount : user.acceptorRatingCount
  const rawAverage = await modal.prompt(`调整${label}`, '请输入新的平均分，范围 0-5', String(Number(currentAverage || 0).toFixed(1)))
  if (rawAverage === false) return
  if (rawAverage === null || rawAverage === undefined || !String(rawAverage).trim()) return
  const averageRating = Number.parseFloat(String(rawAverage).trim())
  if (Number.isNaN(averageRating) || averageRating < 0 || averageRating > 5) {
    await modal.alert('调整失败', '平均分必须在0到5之间')
    return
  }
  const rawCount = await modal.prompt(`调整${label}`, '请输入评分次数，0 表示暂无评分', String(currentCount || 0))
  if (rawCount === false) return
  if (rawCount === null || rawCount === undefined || !String(rawCount).trim()) return
  const ratingCount = Number.parseInt(String(rawCount).trim(), 10)
  if (Number.isNaN(ratingCount) || ratingCount < 0) {
    await modal.alert('调整失败', '评分次数不能为负数')
    return
  }
  const reason = await modal.prompt(`调整${label}`, '请输入调整原因', '评分申诉处理')
  if (reason === false) return
  try {
    await adminAPI.adjustRating(user.id, {
      role,
      averageRating,
      ratingCount,
      reason: reason || '评分申诉处理',
    })
    await search()
  } catch (err) {
    modal.alert('调整失败', errorMessage(err))
  }
}

search()
</script>

<style scoped>
.user-card { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.user-main { min-width: 0; }
.rating-row { display: flex; flex-wrap: wrap; gap: 8px 16px; margin-top: 6px; font-size: 13px; color: var(--text-secondary); font-weight: 700; }
.actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 8px; }
@media (max-width: 820px) {
  .user-card { align-items: flex-start; flex-direction: column; }
  .actions { justify-content: flex-start; }
}
</style>
