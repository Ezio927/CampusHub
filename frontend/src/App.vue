<template>
  <AppLayout>
    <router-view />
  </AppLayout>
</template>
<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/layout/AppLayout.vue'
const auth = useAuthStore()
onMounted(() => auth.restoreSession())
</script>
