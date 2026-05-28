<template>
  <div class="layout">
    <NavBar />
    <main><slot /></main>
    <TabBar />
  </div>
</template>
<script setup>
import NavBar from './NavBar.vue'
import TabBar from './TabBar.vue'
</script>
<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
main { flex: 1; }
</style>
