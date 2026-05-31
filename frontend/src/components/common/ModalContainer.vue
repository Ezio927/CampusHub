<template>
  <teleport to="body">
    <div v-if="state.show" class="modal-overlay" @click.self="onCancel">
      <div class="modal-card">
        <h2 class="modal-title">{{ state.title }}</h2>
        <p v-if="state.message" class="modal-msg">{{ state.message }}</p>
        <input
          v-if="state.type==='prompt'"
          v-model="state.inputValue"
          :placeholder="state.inputPlaceholder"
          class="modal-input"
          @keyup.enter="onConfirm"
        />
        <div class="modal-actions">
          <button v-if="state.type!=='alert'" class="modal-btn modal-btn-cancel" @click="onCancel">取消</button>
          <button class="modal-btn modal-btn-ok" @click="onConfirm">{{ state.type==='confirm'||state.type==='prompt'?'确认':'知道了' }}</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { useModal } from '@/composables/useModal'
const { state, close } = useModal()

function onConfirm() {
  if (state.type === 'prompt') {
    close(state.inputValue || null)
  } else {
    close(true)
  }
}

function onCancel() {
  close(state.type === 'prompt' ? null : false)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .42);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.modal-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px 20px;
  max-width: 380px;
  width: 100%;
  box-shadow: 0 16px 48px rgba(0, 0, 0, .18);
  animation: modalIn .18s ease-out;
}
@keyframes modalIn {
  from { opacity: .5; transform: scale(.94) translateY(8px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.modal-title {
  font-size: 17px;
  font-weight: 800;
  margin-bottom: 8px;
}
.modal-msg {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  white-space: pre-wrap;
}
.modal-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 14px;
  margin-bottom: 16px;
  outline: none;
}
.modal-input:focus { border-color: var(--primary); }
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.modal-btn {
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all .15s;
}
.modal-btn-cancel {
  background: #f1f5f9;
  color: var(--text-secondary);
}
.modal-btn-cancel:hover { background: #e2e8f0; }
.modal-btn-ok {
  background: var(--primary);
  color: #fff;
}
.modal-btn-ok:hover { filter: brightness(1.1); }
</style>
