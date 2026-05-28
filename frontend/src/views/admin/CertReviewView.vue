<template>
  <div class="page-container">
    <h2>认证审核</h2>
    <div v-if="list.length===0" class="empty-state">暂无待审核认证</div>
    <div v-for="c in list" :key="c.id" class="card flex-between">
      <div><strong>用户ID: {{ c.userId }}</strong><br /><small>{{ c.studentId }} · {{ c.certType }} · {{ c.status }}</small></div>
      <div class="flex-row gap-sm">
        <button class="btn-primary btn-sm" @click="review(c.id, true)">通过</button>
        <button class="btn-danger btn-sm" @click="review(c.id, false)">驳回</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { certAPI } from '@/api/certifications'
const list = ref([])
async function load() { try { const r = await certAPI.getMine(); if (r.data) list.value = [r.data] } catch { list.value = [] } }
async function review(id, approve) {
  const reason = approve ? null : prompt('驳回原因')
  await certAPI.review(id, { approve, reason })
  load()
}
load()
</script>
<style scoped>@import './CertReviewView.css';</style>
