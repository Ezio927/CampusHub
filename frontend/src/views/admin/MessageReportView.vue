<template>
  <div class="page">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">私信举报</h1>
    <div v-if="list.length===0" class="empty">暂无待审核举报</div>
    <div v-for="item in list" :key="item.id" class="card">
      <div class="flex-between mb-sm">
        <strong>举报 #{{ item.id }}</strong>
        <span class="badge" style="background:#ef4444">{{ categoryText(item.category) }}</span>
      </div>
      <p class="text-sm text-muted">举报人 {{ item.reporterId }} · 被举报用户 {{ item.targetUserId }} · 消息 {{ item.messageId }}</p>
      <div class="snapshot">
        <button v-if="imageSource(item)" type="button" class="snapshot-image" @click="imageViewer=imageSource(item)">
          <img :src="imageSource(item)" alt="被举报图片" />
        </button>
        <template v-else>{{ item.messageSnapshot || '无消息快照' }}</template>
      </div>
      <p v-if="item.reason" class="mt-sm">说明：{{ item.reason }}</p>
      <p v-if="item.evidenceUrls?.length" class="mt-sm text-sm text-muted">证据：{{ item.evidenceUrls.join('，') }}</p>
      <div class="flex gap-sm mt-md">
        <button class="btn-primary btn-sm" @click="review(item.id,true)">属实</button>
        <button class="btn-outline btn-sm" @click="review(item.id,false)">驳回</button>
      </div>
    </div>
    <div v-if="imageViewer" class="image-viewer" @click.self="imageViewer=''">
      <button type="button" class="viewer-close" @click="imageViewer=''">关闭</button>
      <img :src="imageViewer" alt="被举报图片" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminAPI } from '@/api/admin'
import { useModal } from '@/composables/useModal'
const modal = useModal()

const list = ref([])
const imageViewer = ref('')
const labels = {
  SEXUAL: '色情擦边',
  ABUSE: '骂人',
  POLITICAL: '涉政讨论',
  LINK_OR_QR: '外链/二维码',
  CONTACT_INFO: '联系方式',
}

async function load() {
  try { const res = await adminAPI.messageReports(); list.value = res.data || [] } catch { list.value = [] }
}

async function review(id, approve) {
  const resolution = await modal.prompt('审核举报', '请输入审核说明', '选填')
  if (resolution === false) return
  try {
    await adminAPI.reviewMessageReport(id, { approve, resolution: resolution || '' })
    await load()
  } catch (e) {
    modal.alert('操作失败', e.message)
  }
}

function categoryText(value) {
  return labels[value] || value
}

function imageSource(item) {
  if (item?.messageImageUrl) return item.messageImageUrl
  if (isImageData(item?.messageSnapshot)) return item.messageSnapshot
  return ''
}

function isImageData(value) {
  return typeof value === 'string' && value.startsWith('data:image/')
}

onMounted(load)
</script>

<style scoped>
.snapshot {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  white-space: pre-wrap;
  word-break: break-word;
  border: 2px solid black;
}
.snapshot-image {
  display: block;
  width: 180px;
  height: 132px;
  padding: 0;
  border: 2px solid black;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.snapshot-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-viewer {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  background: rgba(15,23,42,.82);
}
.image-viewer img {
  max-width: min(960px, 96vw);
  max-height: 86vh;
  border-radius: 12px;
  background: #fff;
  border: 2px solid black;
}
.viewer-close {
  position: absolute;
  top: 18px;
  right: 18px;
  background: #fff;
  color: var(--text);
}
</style>
