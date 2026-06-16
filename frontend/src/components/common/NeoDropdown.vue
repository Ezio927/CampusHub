<template>
  <div class="neo-dropdown" ref="containerRef">
    <button
      type="button"
      class="neo-dropdown-trigger"
      :class="{ open: isOpen }"
      @click="toggle"
      @blur="onBlur"
    >
      <span>{{ selectedLabel || placeholder }}</span>
      <span class="neo-arrow" :class="{ open: isOpen }">▼</span>
    </button>
    <div v-if="isOpen" class="neo-dropdown-panel">
      <div
        v-for="opt in options"
        :key="opt.value"
        class="neo-dropdown-item"
        :class="{ selected: modelValue === opt.value }"
        @mousedown.prevent="select(opt.value)"
      >
        {{ opt.label }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, required: true },
  placeholder: { type: String, default: '请选择' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const containerRef = ref(null)
const selectedLabel = ref(null)

watch(() => props.modelValue, () => {
  const o = props.options.find(o => o.value === props.modelValue)
  selectedLabel.value = o?.label || null
}, { immediate: true })

function toggle() {
  isOpen.value = !isOpen.value
}

function onBlur(e) {
  if (!containerRef.value?.contains(e.relatedTarget)) {
    isOpen.value = false
  }
}

function select(value) {
  emit('update:modelValue', value)
  emit('change', value)
  isOpen.value = false
}
</script>

<style scoped>
.neo-dropdown {
  position: relative;
  display: inline-block;
}

.neo-dropdown-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  min-width: 140px;
  padding: 10px 14px;
  font-size: 15px;
  font-weight: 700;
  font-family: inherit;
  background: #fff;
  color: #111827;
  border: 2px solid #000;
  border-radius: 8px;
  box-shadow: 4px 4px 0px #000;
  cursor: pointer;
  transition: all 0.15s ease;
  text-align: left;
  white-space: nowrap;
}

.neo-dropdown-trigger.open {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #000;
}

.neo-dropdown-trigger:hover {
  background: #f5f5f5;
}

.neo-arrow {
  margin-left: auto;
  font-size: 10px;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.neo-arrow.open {
  transform: rotate(180deg);
}

.neo-dropdown-panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 999;
  min-width: 100%;
  background: #fff;
  border: 2px solid #000;
  border-radius: 8px;
  box-shadow: 4px 4px 0px #000;
  overflow: hidden;
}

.neo-dropdown-item {
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  cursor: pointer;
  transition: all 0.1s ease;
  border-bottom: 1px solid #e5e7eb;
}

.neo-dropdown-item:last-child {
  border-bottom: none;
}

.neo-dropdown-item:hover {
  background: #FFD700;
  padding-left: 18px;
  border-bottom-color: #000;
}

.neo-dropdown-item.selected {
  background: #e5e7eb;
  font-weight: 700;
}
</style>
