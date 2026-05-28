<template>
  <div class="page-container">
    <div class="profile-header">
      <div class="profile-avatar" :style="{background:avatarColor}">{{ auth.currentUser?.username?.[0]?.toUpperCase() }}</div>
      <div class="profile-name">{{ auth.currentUser?.username }}</div>
      <p class="text-secondary text-sm">{{ auth.currentUser?.studentId }}</p>
      <p class="mt-sm">信用分 <span :style="{ color: scoreColor, fontWeight:'700',fontSize:'20px' }">{{ auth.currentUser?.creditScore }}</span> · {{ statusText }}</p>
    </div>
    <div class="profile-section">
      <h4>编辑资料</h4>
      <div class="form-group"><label>姓名</label><input v-model="edit.realName" :placeholder="auth.currentUser?.realName" /></div>
      <div class="form-group"><label>手机</label><input v-model="edit.phone" :placeholder="auth.currentUser?.phone" /></div>
      <div class="form-group"><label>邮箱</label><input v-model="edit.email" :placeholder="auth.currentUser?.email" /></div>
      <button class="btn-primary btn-sm" @click="update">保存修改</button>
    </div>
    <div class="profile-section">
      <h4>实名认证</h4>
      <template v-if="!cert">
        <div class="form-group"><label>认证类型</label><select v-model="certForm.certType"><option value="STUDENT_ID">学生证</option><option value="STUDENT_CARD">校园卡</option></select></div>
        <div class="form-group"><label>学号</label><input v-model="certForm.studentId" /></div>
        <div class="form-group"><label>证件图片URL</label><input v-model="certForm.cardImageUrl" /></div>
        <button class="btn-primary btn-sm" @click="submitCert">提交认证</button>
      </template>
      <p v-else class="text-bold">认证状态: <span :style="{color:cert.status==='APPROVED'?'var(--success)':'var(--warning)'}">{{ cert.status }}</span></p>
    </div>
    <button class="btn-danger logout-btn" @click="handleLogout">退出登录</button>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api/users'
import { certAPI } from '@/api/certifications'
const router = useRouter()
const auth = useAuthStore()
const edit = reactive({ realName: '', phone: '', email: '' })
const cert = ref(null)
const certForm = reactive({ certType: 'STUDENT_ID', studentId: '', cardImageUrl: '' })
const avatarColor = computed(() => { const colors = ['#6366f1','#8b5cf6','#ec4899','#f43f5e','#f97316']; return colors[(auth.currentUser?.username?.charCodeAt(0)||0)%colors.length] })
const scoreColor = computed(() => (auth.currentUser?.creditScore >= 80 ? 'var(--success)' : auth.currentUser?.creditScore >= 60 ? 'var(--warning)' : 'var(--danger)'))
const statusText = computed(() => ({ ACTIVE: '正常', BANNED: '已封禁', PENDING_CERT: '待认证' }[auth.currentUser?.status] || auth.currentUser?.status))
async function update() { const d = {}; if (edit.realName) d.realName = edit.realName; if (edit.phone) d.phone = edit.phone; if (edit.email) d.email = edit.email; try { await userAPI.updateProfile(d); await auth.fetchProfile() } catch (e) { alert(e.message) } }
async function submitCert() { try { await certAPI.submit({ ...certForm }); await loadCert() } catch (e) { alert(e.message) } }
async function loadCert() { try { const r = await certAPI.getMine(); cert.value = r.data } catch { cert.value = null } }
function handleLogout() { auth.logout(); router.push('/login') }
onMounted(async () => { await auth.fetchProfile(); await loadCert() })
</script>
<style scoped>@import './ProfileView.css';</style>
