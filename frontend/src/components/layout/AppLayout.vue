<template>
  <div class="layout">
    <NavBar />
    <main class="main"><slot /></main>
  </div>
</template>
<script setup>
import NavBar from './NavBar.vue'
</script>
<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.main { flex: 1; min-width: 0; width: 80%; margin: 0 auto; }
</style>
