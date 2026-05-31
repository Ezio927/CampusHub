<template>
  <div class="page"><a href="#" class="back" @click.prevent="$router.back()">←</a>
    <h1 class="page-title">🪪 认证审核</h1>
    <div v-if="list.length===0" class="empty">暂无待审核认证</div>
    <div v-for="c in list" :key="c.id" class="card flex-between">
      <div><div class="text-bold">用户ID: {{ c.userId }}</div><div class="text-sm text-muted">{{ c.studentId }} · {{ c.certType }} · {{ c.status }}</div></div>
      <div class="flex gap-sm">
        <button class="btn-primary btn-sm" @click="review(c.id,true)">通过</button>
        <button class="btn-danger btn-sm" @click="review(c.id,false)">驳回</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref} from 'vue';import {certAPI} from '@/api/certifications'
import { useModal } from '@/composables/useModal'
const modal = useModal()
const list=ref([])
async function load(){try{const r=await certAPI.pending();list.value=r.data||[]}catch{list.value=[]}}
async function review(id,approve){const reason=approve?null:await modal.prompt('驳回认证','请输入驳回原因','请填写驳回原因');if(reason===false)return;await certAPI.review(id,{approve,reason});load()}
load()
</script>
