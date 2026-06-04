<template>
  <div class="page chat-page">
    <a href="#" class="back" @click.prevent="$router.back()">←</a>

    <div class="chat-head">
      <button type="button" class="avatar" :class="{hasAvatar:conversation?.targetUser?.avatarUrl}" @click="openImageViewer(conversation?.targetUser?.avatarUrl)">
        <img v-if="conversation?.targetUser?.avatarUrl" :src="conversation.targetUser.avatarUrl" alt="对方头像" />
        <span v-else>{{ userInitial(conversation?.targetUser) }}</span>
      </button>
      <div class="head-main">
        <div class="chat-name">{{ conversation?.targetUser?.username || '私信' }}</div>
        <div v-if="conversation?.relatedDemandTitle" class="chat-context">{{ conversation.relatedDemandTitle }}</div>
        <div v-if="conversation?.blockedByTarget" class="state danger">对方已拉黑你</div>
        <div v-else-if="conversation?.blockedByMe" class="state">你已拉黑对方</div>
      </div>
      <button v-if="conversation?.blockedByMe" type="button" class="btn-outline btn-sm" @click="unblockPeer">解除拉黑</button>
      <button v-else type="button" class="btn-outline btn-sm" @click="blockPeer">拉黑</button>
    </div>

    <div class="search-row">
      <input v-model="searchKeyword" placeholder="搜索当前会话" @keyup.enter="searchMessages" />
      <button type="button" class="btn-outline btn-sm" @click="searchMessages">搜索</button>
      <button v-if="searchResults.length" type="button" class="btn-outline btn-sm" @click="clearSearch">清除</button>
    </div>
    <div v-if="searchResults.length" class="search-results">
      <button v-for="item in searchResults" :key="item.id" type="button" @click="jumpToMessage(item.id)">
        <span>{{ formatTime(item.createdAt) }}</span>
        <strong>{{ item.messageType === 'IMAGE' ? '[图片]' : item.content }}</strong>
      </button>
    </div>

    <div class="history">
      <div v-if="msgs.length===0" class="empty compact">
        <p>暂无消息</p>
      </div>
      <div
        v-for="m in msgs"
        :id="'msg-'+m.id"
        :key="m.id"
        class="msg-row"
        :class="{ mine: m.senderId===auth.user?.id, recalled: m.status==='RECALLED', system: m.messageType==='SYSTEM' }"
      >
        <div class="msg-bubble">
          <div v-if="m.quotedContent" class="quote-in-msg">{{ m.quotedContent }}</div>
          <button v-if="m.messageType==='IMAGE' && m.status!=='RECALLED'" type="button" class="image-link" @click="openImageViewer(m.content)">
            <img :src="m.content" alt="私信图片" />
          </button>
          <p v-else>{{ m.content }}</p>
          <div class="msg-meta">
            <small>{{ formatTime(m.createdAt) }}</small>
            <small v-if="m.senderId===auth.user?.id && m.status!=='RECALLED'">{{ m.read ? '已读' : '已发送' }}</small>
            <button v-if="m.status!=='RECALLED' && m.messageType!=='SYSTEM'" type="button" @click="quote(m)">引用</button>
            <button v-if="canRecall(m)" type="button" @click="recall(m)">撤回</button>
            <button v-if="m.status!=='RECALLED' && m.messageType!=='SYSTEM'" type="button" @click="openReport(m)">举报</button>
          </div>
        </div>
      </div>
      <div ref="bottom"></div>
    </div>

    <div v-if="reportTarget" class="report-box">
      <div class="flex-between mb-sm">
        <strong>举报消息</strong>
        <button type="button" class="link-btn" @click="closeReport">关闭</button>
      </div>
      <div class="report-target">
        <button v-if="reportTarget.messageType === 'IMAGE'" type="button" class="report-image" @click="openImageViewer(reportTarget.content)">
          <img :src="reportTarget.content" alt="举报图片" />
        </button>
        <template v-else>{{ reportTarget.content }}</template>
      </div>
      <select v-model="reportForm.category" class="mt-sm">
        <option value="SEXUAL">色情擦边玩笑</option>
        <option value="ABUSE">骂人（轻度/重度）</option>
        <option value="POLITICAL">涉政讨论</option>
        <option value="LINK_OR_QR">外链/二维码</option>
        <option value="CONTACT_INFO">发布手机号/微信号/QQ号</option>
      </select>
      <textarea v-model="reportForm.reason" rows="3" placeholder="补充说明（选填）"></textarea>
      <input v-model="reportForm.evidence" placeholder="证据链接，多个用逗号分隔（选填）" />
      <button type="button" class="btn-primary btn-sm" :disabled="reporting" @click="submitReport">
        {{ reporting ? '提交中' : '提交举报' }}
      </button>
    </div>

    <form class="composer" @submit.prevent="send">
      <div v-if="quoted" class="quote-preview">
        <span>引用：{{ quoted.messageType === 'IMAGE' ? '[图片]' : quoted.content }}</span>
        <button type="button" @click="quoted=null">取消</button>
      </div>
      <div v-if="imagePreview" class="image-preview">
        <img :src="imagePreview" alt="待发送图片" />
        <button type="button" @click="clearImage">移除图片</button>
      </div>
      <div class="emoji-row">
        <button v-for="emoji in emojis" :key="emoji" type="button" @click="insertEmoji(emoji)">{{ emoji }}</button>
      </div>
      <div class="compose-row">
        <textarea
          v-model="txt"
          :placeholder="placeholder"
          maxlength="500"
          rows="2"
          :disabled="inputDisabled"
          @keydown.enter="handleComposerEnter"
        ></textarea>
        <div class="compose-actions">
          <label class="btn-outline btn-sm file-btn">
            图片
            <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" @change="pickImage" />
          </label>
          <button class="btn-primary btn-sm" :disabled="!canSend">{{ sending ? '发送中' : '发送' }}</button>
        </div>
      </div>
      <p v-if="err" class="form-error">{{ err }}</p>
    </form>

    <div v-if="imageViewer" class="image-viewer" @click.self="closeImageViewer">
      <button type="button" class="viewer-close" @click="closeImageViewer">关闭</button>
      <img :src="imageViewer" alt="私信图片" />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/api/chat'
import { uploadImage } from '@/api/upload'
import { errorMessage } from '@/utils/errors'
import { userInitial } from '@/utils/userDisplay'
import { useModal } from '@/composables/useModal'

const route = useRoute()
const auth = useAuthStore()
const modal = useModal()
const msgs = ref([])
const txt = ref('')
const conversation = ref(null)
const sending = ref(false)
const bottom = ref(null)
const quoted = ref(null)
const reportTarget = ref(null)
const reporting = ref(false)
const err = ref('')
const imagePreview = ref('')
const imageFile = ref(null)
const imageViewer = ref('')
const searchKeyword = ref('')
const searchResults = ref([])
const reportForm = reactive({ category: 'ABUSE', reason: '', evidence: '' })
const emojis = ['😀','😂','😊','👍','🙏','🎉','❤️','😅','👌','🔥','🌟','📌']
let syncTimer = null

const inputDisabled = computed(() => conversation.value?.blockedByTarget || conversation.value?.blockedByMe)
const placeholder = computed(() => {
  if (conversation.value?.blockedByTarget) return '对方已拉黑你，无法发送'
  if (conversation.value?.blockedByMe) return '你已拉黑对方，解除后可继续发送'
  return '输入消息...'
})
const canSend = computed(() => !inputDisabled.value && !sending.value && (txt.value.trim() || imagePreview.value))

async function send() {
  if (!canSend.value) return
  sending.value = true
  err.value = ''
  try {
    const isImage = Boolean(imagePreview.value)
    let content = txt.value.trim()
    if (isImage) {
      const uploadRes = await uploadImage(imageFile.value)
      content = uploadRes.data.url
    }
    const res = await chatAPI.send(route.params.id, {
      content,
      messageType: isImage ? 'IMAGE' : 'TEXT',
      quotedMessageId: quoted.value?.id || null,
      clientMessageId: createClientMessageId(),
    })
    appendOrReplace(res.data)
    txt.value = ''
    quoted.value = null
    clearImage()
    await markReadToLast()
    await loadConversation()
    if (res.data?.riskWarnings?.length) modal.alert('发送提醒', res.data.riskWarnings.join('\n'))
    await scrollBottom()
  } catch (e) {
    err.value = errorMessage(e, '发送失败')
  } finally {
    sending.value = false
  }
}

async function loadConversation() {
  const res = await chatAPI.detail(route.params.id)
  conversation.value = res.data
  refreshReadState()
}

async function loadMessages() {
  const res = await chatAPI.messages(route.params.id, { page: 0, size: 100 })
  msgs.value = res.data || []
  await markReadToLast()
  await scrollBottom()
}

async function syncNewMessages() {
  if (!msgs.value.length) {
    await loadConversation().catch(() => {})
    return
  }
  const lastId = msgs.value[msgs.value.length - 1]?.id || 0
  try {
    await loadConversation()
    const res = await chatAPI.messages(route.params.id, { afterId: lastId, size: 100 })
    const incoming = res.data || []
    if (incoming.length) {
      incoming.forEach(appendOrReplace)
      await markReadToLast()
      await scrollBottom()
    }
  } catch {
    // 静默等待下一轮同步
  }
}

async function markReadToLast() {
  const lastId = msgs.value[msgs.value.length - 1]?.id
  await chatAPI.read(route.params.id, lastId).catch(() => {})
}

function appendOrReplace(message) {
  if (!message?.id) return
  const index = msgs.value.findIndex(item => item.id === message.id)
  if (index >= 0) msgs.value[index] = message
  else msgs.value.push(message)
  msgs.value.sort((a, b) => a.id - b.id)
}

function refreshReadState() {
  const peerRead = conversation.value?.peerLastReadMessageId || 0
  msgs.value = msgs.value.map(item => {
    if (item.senderId !== auth.user?.id) return item
    return { ...item, read: item.id <= peerRead }
  })
}

function formatTime(value) {
  if (!value) return ''
  return value.substring(0, 16).replace('T', ' ')
}

function quote(message) {
  quoted.value = message
}

function canRecall(message) {
  if (message.senderId !== auth.user?.id || message.status === 'RECALLED' || !message.createdAt) return false
  return Date.now() - new Date(message.createdAt).getTime() <= 2 * 60 * 1000
}

async function recall(message) {
  try {
    const res = await chatAPI.recall(route.params.id, message.id)
    appendOrReplace(res.data)
    await loadConversation()
  } catch (e) {
    modal.alert('操作失败', errorMessage(e,'撤回失败'))
  }
}

async function blockPeer() {
  if (!await modal.confirm('拉黑对方', '拉黑后，对方将不能继续给你发送私信。')) return
  try {
    const res = await chatAPI.block(route.params.id)
    conversation.value = res.data
  } catch (e) {
    modal.alert('操作失败', errorMessage(e,'拉黑失败'))
  }
}

async function unblockPeer() {
  try {
    const res = await chatAPI.unblock(route.params.id)
    conversation.value = res.data
  } catch (e) {
    modal.alert('操作失败', errorMessage(e,'解除拉黑失败'))
  }
}

async function searchMessages() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) { searchResults.value = []; return }
  try {
    const res = await chatAPI.search(route.params.id, keyword)
    searchResults.value = res.data || []
  } catch (e) {
    modal.alert('操作失败', errorMessage(e,'搜索失败'))
  }
}

function clearSearch() {
  searchKeyword.value = ''
  searchResults.value = []
}

function jumpToMessage(id) {
  document.getElementById('msg-' + id)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function openReport(message) {
  reportTarget.value = message
  reportForm.category = 'ABUSE'
  reportForm.reason = ''
  reportForm.evidence = ''
}

function closeReport() {
  reportTarget.value = null
}

async function submitReport() {
  if (!reportTarget.value) return
  reporting.value = true
  try {
    await chatAPI.report(route.params.id, reportTarget.value.id, {
      category: reportForm.category,
      reason: reportForm.reason.trim(),
      evidenceUrls: reportForm.evidence.split(/[,，\n]/).map(item => item.trim()).filter(Boolean),
    })
    closeReport()
    modal.alert('举报已提交', '管理员会进行审核')
  } catch (e) {
    modal.alert('操作失败', errorMessage(e,'提交失败'))
  } finally {
    reporting.value = false
  }
}

function insertEmoji(emoji) {
  if (inputDisabled.value) return
  txt.value += emoji
}

function handleComposerEnter(event) {
  if (event.shiftKey || event.isComposing) return
  event.preventDefault()
  send()
}

function pickImage(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) { err.value = '请选择图片文件'; return }
  if (file.size > 3 * 1024 * 1024) { err.value = '图片需小于3MB'; return }
  imageFile.value = file
  const reader = new FileReader()
  reader.onload = () => {
    imagePreview.value = String(reader.result || '')
    err.value = ''
  }
  reader.readAsDataURL(file)
}

function clearImage() {
  imagePreview.value = ''
  imageFile.value = null
}

function openImageViewer(src) {
  imageViewer.value = src || ''
}

function closeImageViewer() {
  imageViewer.value = ''
}

function createClientMessageId() {
  if (window.crypto?.randomUUID) return window.crypto.randomUUID()
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function scrollBottom() {
  await nextTick()
  bottom.value?.scrollIntoView({ behavior: 'smooth', block: 'end' })
}

onMounted(async () => {
  try { await loadConversation(); await loadMessages() } catch { /* page shows empty state */ }
  syncTimer = window.setInterval(syncNewMessages, 5000)
})

onUnmounted(() => {
  if (syncTimer) window.clearInterval(syncTimer)
})
</script>

<style scoped>
.chat-page { max-width: 820px; }
.chat-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  margin-bottom: 10px;
  border: 2px solid black;
  border-radius: 10px;
  background: #fff;
}
.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
  border: 2px solid black;
  padding: 0;
  overflow: hidden;
}
.avatar.hasAvatar { background: #fff; }
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.head-main { flex: 1; min-width: 0; }
.chat-name { font-size: 16px; font-weight: 900; }
.chat-context, .state { margin-top: 2px; color: var(--text-muted); font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.state.danger { color: var(--danger); }
.search-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.search-row input { flex: 1; }
.search-results {
  display: grid;
  gap: 6px;
  margin-bottom: 10px;
}
.search-results button {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 8px;
  padding: 8px 10px;
  border: 2px solid black;
  border-radius: 8px;
  background: #fff;
  text-align: left;
}
.search-results span { color: var(--text-muted); font-size: 12px; }
.search-results strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.history {
  min-height: 420px;
  max-height: 58vh;
  overflow-y: auto;
  padding: 16px;
  border: 2px solid black;
  border-radius: 10px;
  background: #fff;
}
.compact { padding: 120px 20px; }
.msg-row {
  display: flex;
  margin-bottom: 10px;
  scroll-margin: 80px;
}
.msg-row.mine { justify-content: flex-end; }
.msg-row.system { justify-content: center; }
.msg-bubble {
  max-width: min(76%, 520px);
  padding: 10px 12px;
  border-radius: 10px;
  background: #f1f5f9;
  color: var(--text);
  border: 2px solid black;
}
.mine .msg-bubble { background: var(--primary); color: #fff; }
.system .msg-bubble {
  max-width: min(88%, 560px);
  background: #f8fafc;
  color: var(--text-secondary);
  border: 2px solid black;
  text-align: center;
}
.recalled .msg-bubble { color: var(--text-muted); font-style: italic; border: 2px solid #e5e7eb; }
.msg-bubble p { white-space: pre-wrap; word-break: break-word; }
.image-link {
  display: block;
  padding: 0;
  border: 0;
  border-radius: 6px;
  background: transparent;
  line-height: 0;
}
.image-link img {
  display: block;
  max-width: 220px;
  max-height: 180px;
  border-radius: 6px;
  object-fit: cover;
  border: 2px solid black;
}
.quote-in-msg {
  margin-bottom: 8px;
  padding: 6px 8px;
  border-left: 3px solid rgba(99,102,241,.55);
  border-radius: 6px;
  background: rgba(255,255,255,.62);
  color: var(--text-secondary);
  font-size: 12px;
  word-break: break-word;
}
.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.msg-meta small { color: var(--text-muted); font-size: 11px; }
.msg-meta button, .link-btn {
  padding: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  border: none;
}
.msg-meta button:hover, .link-btn:hover { color: var(--primary); }
.mine .msg-meta small, .mine .msg-meta button { color: rgba(255,255,255,.78); }
.report-box, .composer {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
  padding: 12px;
  border: 2px solid black;
  border-radius: 10px;
  background: #fff;
}
.report-target {
  padding: 10px;
  border-radius: 8px;
  background: #f8fafc;
  color: var(--text-secondary);
  word-break: break-word;
  border: 2px solid black;
}
.report-image {
  display: block;
  width: 140px;
  height: 104px;
  padding: 0;
  border: 2px solid black;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
.report-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.quote-preview, .image-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--primary-light);
  color: var(--primary);
  font-size: 13px;
  border: 2px solid black;
}
.quote-preview span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.quote-preview button, .image-preview button {
  min-width: auto;
  padding: 0;
  background: transparent;
  color: var(--primary);
  border: none;
}
.image-preview img { width: 56px; height: 42px; border-radius: 6px; object-fit: cover; border: 2px solid black; }
.emoji-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.emoji-row button {
  width: 30px;
  height: 30px;
  padding: 0;
  border: 2px solid black;
  border-radius: 6px;
  background: #fff;
  line-height: 26px;
  font-size: 16px;
}
.compose-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: end;
}
.compose-row textarea {
  resize: vertical;
  min-height: 52px;
  max-height: 140px;
}
.compose-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.file-btn { position: relative; overflow: hidden; cursor: pointer; }
.file-btn input {
  position: absolute;
  inset: 0;
  opacity: 0;
  pointer-events: none;
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
@media (max-width: 640px) {
  .chat-head { align-items: flex-start; }
  .search-row, .compose-row { grid-template-columns: 1fr; display: grid; }
  .compose-actions { justify-content: flex-end; }
  .msg-bubble { max-width: 88%; }
  .search-results button { grid-template-columns: 1fr; }
}
</style>
