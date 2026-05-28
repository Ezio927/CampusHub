<template>
  <div>
    <NavBar />
    <main class="page-container"><slot /></main>
    <TabBar />
  </div>
</template>
<script setup>
import NavBar from './NavBar.vue'
import TabBar from './TabBar.vue'
</script>
