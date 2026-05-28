<template>
  <nav class="tabs">
    <router-link to="/" class="tab" :class="{on:$route.path==='/'}"><span class="ico">🏠</span>首页</router-link>
    <router-link to="/orders" class="tab" :class="{on:$route.path==='/orders'}"><span class="ico">📋</span>订单</router-link>
    <router-link to="/profile" class="tab" :class="{on:$route.path==='/profile'}"><span class="ico">👤</span>我的</router-link>
  </nav>
</template>
<style scoped>
.tabs { display: none; position: fixed; bottom: 0; left: 0; right: 0; background: rgba(255,255,255,.92); backdrop-filter: blur(12px); border-top: 1px solid var(--border); z-index: 100; padding: 6px 0; }
.tab { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 4px; color: var(--text-muted); font-size: 10px; font-weight: 500; }
.tab.on { color: var(--primary); font-weight: 700; }
.ico { font-size: 20px; }
@media (max-width: 768px) { .tabs { display: flex; } }
</style>
