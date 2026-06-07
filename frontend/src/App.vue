<template>
  <AppLayout>
    <router-view />
  </AppLayout>
  <ModalContainer />
</template>
<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/layout/AppLayout.vue'
import ModalContainer from '@/components/common/ModalContainer.vue'
const auth = useAuthStore()
onMounted(() => auth.restoreSession())
</script>
