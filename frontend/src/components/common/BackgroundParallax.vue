<template>
  <div class="bg-parallax" ref="bgRef">
    <div
      v-for="el in elements"
      :key="el.id"
      class="bg-el"
      :style="el.style"
      :ref="setElRef"
    >
      <span v-html="el.html" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const elDomRefs = []
function setElRef(el) { if (el) elDomRefs.push(el) }

function genId() { return 'el-' + Math.random().toString(36).slice(2, 8) }

const objects = [
  // book
  { html: '<svg viewBox="0 0 16 16" width="32" height="32"><rect x="2" y="1" width="5" height="14" rx="0.5" fill="#6366f1" stroke="#000" stroke-width="1.5"/><rect x="9" y="1" width="5" height="14" rx="0.5" fill="#818cf8" stroke="#000" stroke-width="1.5"/><line x1="7" y1="1" x2="7" y2="15" stroke="#000" stroke-width="1"/></svg>' },
  // pencil
  { html: '<svg viewBox="0 0 16 16" width="28" height="28"><rect x="2" y="1" width="12" height="3" rx="0.5" fill="#f59e0b" stroke="#000" stroke-width="1.5"/><polygon points="4,4 12,4 15,14 1,14" fill="#fbbf24" stroke="#000" stroke-width="1.5" stroke-linejoin="round"/><rect x="6" y="13" width="4" height="3" rx="0.5" fill="#fcd34d" stroke="#000" stroke-width="1"/></svg>' },
  // graduation cap
  { html: '<svg viewBox="0 0 16 16" width="30" height="30"><polygon points="1,6 8,2 15,6 8,8" fill="#111827" stroke="#000" stroke-width="1.2" stroke-linejoin="round"/><rect x="3" y="6" width="10" height="1.5" rx="0.5" fill="#111827" stroke="#000" stroke-width="1"/><line x1="7" y1="7.5" x2="7" y2="14" stroke="#000" stroke-width="1"/><rect x="4" y="14" width="6" height="2" rx="1" fill="#ef4444" stroke="#000" stroke-width="1"/></svg>' },
  // calculator
  { html: '<svg viewBox="0 0 16 16" width="28" height="32"><rect x="2" y="1" width="12" height="14" rx="1.5" fill="#d1d5db" stroke="#000" stroke-width="1.5"/><rect x="4" y="3" width="8" height="2.5" rx="0.5" fill="#10b981" stroke="#000" stroke-width="1"/><circle cx="5.5" cy="8" r="1" fill="#374151"/><circle cx="8" cy="8" r="1" fill="#374151"/><circle cx="10.5" cy="8" r="1" fill="#374151"/><circle cx="5.5" cy="10.5" r="1" fill="#374151"/><circle cx="8" cy="10.5" r="1" fill="#374151"/><circle cx="10.5" cy="10.5" r="1" fill="#374151"/></svg>' },
  // clock
  { html: '<svg viewBox="0 0 16 16" width="26" height="26"><circle cx="8" cy="8" r="7" fill="#fff" stroke="#000" stroke-width="1.5"/><line x1="8" y1="8" x2="8" y2="4" stroke="#000" stroke-width="1.5" stroke-linecap="round"/><line x1="8" y1="8" x2="11" y2="10" stroke="#000" stroke-width="1.5" stroke-linecap="round"/><circle cx="8" cy="8" r="1" fill="#ef4444"/></svg>' },
  // four-pointed star
  { html: '<svg viewBox="0 0 16 16" width="22" height="22"><polygon points="8,1 10,6 15,6 11,10 13,15 8,12 3,15 5,10 1,6 6,6" fill="#facc15" stroke="#000" stroke-width="1.3" stroke-linejoin="round"/></svg>' },
  // circle
  { html: '<svg viewBox="0 0 16 16" width="18" height="18"><circle cx="8" cy="8" r="7" fill="#06b6d4" stroke="#000" stroke-width="1.3"/></svg>' },
  // small cross/dot
  { html: '<svg viewBox="0 0 12 12" width="16" height="16"><line x1="1" y1="6" x2="11" y2="6" stroke="#000" stroke-width="1.8" stroke-linecap="round"/><line x1="6" y1="1" x2="6" y2="11" stroke="#000" stroke-width="1.8" stroke-linecap="round"/></svg>' },
]

const elements = ref([])
function randomBetween(a, b) { return a + Math.random() * (b - a) }

function createElements() {
  elDomRefs.length = 0
  const count = 12
  const items = []
  // place mostly in edge/quiet areas
  const positions = [
    [8,85],[92,8],[5,50],[88,55],[12,20],[80,75],
    [45,92],[55,5],[25,80],[72,25],[88,40],[10,70],
  ]
  for (let i = 0; i < count; i++) {
    const tpl = objects[i % objects.length]
    const [left, top] = positions[i]
    const duration = randomBetween(5, 9)
    const delay = randomBetween(0, 6)
    const speed = +(randomBetween(0.01, 0.04).toFixed(3))
    const size = 0.65 + Math.random() * 0.35
    const opacity = 0.5 + Math.random() * 0.25
    const baseRotate = randomBetween(-8, 8)

    items.push({
      id: genId(),
      html: tpl.html,
      speed,
      style: {
        top: top + '%',
        left: left + '%',
        animationDuration: duration + 's',
        animationDelay: delay + 's',
        transform: `scale(${size}) rotate(${baseRotate}deg)`,
        opacity,
      }
    })
  }
  elements.value = items
}

let rafId = null
function onMouseMove(e) {
  if (rafId) return
  rafId = requestAnimationFrame(() => {
    rafId = null
    const x = e.clientX / window.innerWidth - 0.5
    const y = e.clientY / window.innerHeight - 0.5
    elements.value.forEach((el, i) => {
      const dx = x * el.speed * 400
      const dy = y * el.speed * 400
      const base = `scale(${el.style.transform.match(/scale\(([^)]+)\)/)?.[1] || '1'})`
      const rot = el.style.transform.match(/rotate\(([^)]+)\)/)?.[1] || '0deg'
      const domEl = elDomRefs[i]
      if (domEl) {
        domEl.style.transform = `${base} rotate(${rot}) translate(${dx}px, ${dy}px)`
      }
    })
  })
}

onMounted(() => {
  createElements()
  window.addEventListener('mousemove', onMouseMove, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
})
</script>

<style scoped>
.bg-parallax {
  position: fixed;
  inset: 0;
  z-index: -1;
  background: var(--bg);
  overflow: hidden;
  pointer-events: none;
}

.bg-el {
  position: absolute;
  animation: bg-float ease-in-out infinite;
  pointer-events: none;
}

@keyframes bg-float {
  0%, 100% { margin-top: 0; }
  50% { margin-top: -14px; }
}
</style>
