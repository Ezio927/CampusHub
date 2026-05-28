<template>
  <nav class="tabbar">
    <router-link to="/" class="tab" :class="{ active: $route.path === '/' }">首页</router-link>
    <router-link to="/orders" class="tab" :class="{ active: $route.path === '/orders' }">订单</router-link>
    <router-link to="/profile" class="tab" :class="{ active: $route.path === '/profile' }">我的</router-link>
  </nav>
</template>
<style scoped>
.tabbar { display: flex; position: fixed; bottom: 0; left: 0; right: 0; background: var(--card); border-top: 1px solid var(--border); z-index: 100; }
.tab { flex: 1; text-align: center; padding: 10px; color: var(--text-secondary); font-size: 14px; }
.tab.active { color: var(--primary); font-weight: bold; }
</style>
