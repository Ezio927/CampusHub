<template>
  <div class="page">
    <h1 class="page-title">⚖️ 仲裁处理</h1>
    <div v-if="list.length===0" class="empty">暂无待处理仲裁</div>
    <div v-for="t in list" :key="t.id" class="card">
      <p><strong>工单 #{{ t.id }}</strong> · {{ t.status }}</p>
      <p>订单ID: {{ t.orderId }}</p><p>原因: {{ t.reason }}</p>
      <div class="flex gap-sm mt-sm">
        <button class="btn-primary btn-sm" @click="resolve(t.id,'RESOLVE')">处理</button>
        <button class="btn-danger btn-sm" @click="resolve(t.id,'DISMISS')">驳回</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref,onMounted} from 'vue';import {arbAPI} from '@/api/arbitrations'
const list=ref([])
async function resolve(id,action){const resolution=prompt('处理说明');await arbAPI.resolve(id,{action,resolution});load()}
async function load(){const r=await arbAPI.pending();list.value=r.data||[]}
onMounted(load)
</script>
