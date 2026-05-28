<template>
  <nav class="tabbar">
    <router-link to="/" class="tab"><span class="tab-icon">🏠</span><span>首页</span></router-link>
    <router-link to="/orders" class="tab"><span class="tab-icon">📋</span><span>订单</span></router-link>
    <router-link to="/profile" class="tab"><span class="tab-icon">👤</span><span>我的</span></router-link>
  </nav>
</template>
<style scoped>@import './TabBar.css';</style>
