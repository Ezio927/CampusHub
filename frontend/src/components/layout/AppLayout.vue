<template>
  <div class="layout">
    <NavBar />
    <main class="main"><slot /></main>
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-left">
          <div class="footer-brand">
            <span class="footer-logo">CampusHub</span>
            <span class="footer-desc">校园互助平台</span>
          </div>
          <p class="footer-tagline">面向大学生的校园任务互助系统</p>
          <p class="footer-copy">© 2026 南京大学软件工程二课程项目</p>
        </div>
        <div class="footer-right">
          <p class="footer-label">联系与技术</p>
          <p class="footer-info"><a href="mailto:admin@campushub.cn">admin@campushub.cn</a></p>
          <p class="footer-info">Spring Boot 3 · Vue 3 · MySQL · Redis</p>
        </div>
      </div>
    </footer>
  </div>
</template>
<script setup>
import NavBar from './NavBar.vue'
</script>
<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.main { flex: 1; min-width: 0; width: 80%; margin: 0 auto; }
.footer { margin-top: 48px; border-top: 2px solid black; background: var(--card); }
.footer-inner { width: 80%; margin: 0 auto; padding: 28px 0; display: flex; justify-content: space-between; gap: 40px; flex-wrap: wrap; }
.footer-left { display: flex; flex-direction: column; gap: 4px; }
.footer-right { display: flex; flex-direction: column; gap: 4px; text-align: right; }
.footer-brand { display: flex; align-items: baseline; gap: 10px; }
.footer-logo { font-size: 16px; font-weight: 900; color: var(--primary); }
.footer-desc { font-size: 13px; color: var(--text-muted); font-weight: 500; }
.footer-tagline { font-size: 12px; color: var(--text-muted); margin: 2px 0 0; }
.footer-copy { font-size: 12px; color: var(--text-muted); margin-top: 8px; }
.footer-label { font-size: 12px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 1px; }
.footer-info { font-size: 13px; color: var(--text-muted); }
.footer-info a { color: var(--text-muted); }
.footer-info a:hover { color: var(--primary); }
</style>
