<template>
  <div class="layout">
    <SideBar />
    <main class="main"><slot /></main>
  </div>
</template>
<script setup>
import SideBar from './SideBar.vue'
</script>
<style scoped>
.layout { min-height: 100vh; display: flex; }
.main { flex: 1; margin-left: 240px; min-width: 0; }
</style>
