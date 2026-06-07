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
  close(false)
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
  background: var(--card);
  border-radius: 16px;
  padding: 28px 24px 20px;
  max-width: 380px;
  width: 100%;
  border: 2px solid black;
  box-shadow: 4px 4px 0px rgba(0,0,0,1);
  animation: modalIn .18s ease-out;
}
@keyframes modalIn {
  from { opacity: .5; transform: scale(.94) translateY(8px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.modal-title {
  font-size: 17px;
  font-weight: 900;
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
  border: 2px solid black;
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
  transition: all 0.2s ease;
  border: 2px solid black;
}
.modal-btn-cancel {
  background: var(--surface);
  color: var(--text-secondary);
  box-shadow: 4px 4px 0px rgba(0,0,0,1);
}
.modal-btn-cancel:hover { background: var(--border); transform: translate(1px,1px); box-shadow: none; }
.modal-btn-ok {
  background: var(--primary);
  color: #fff;
  box-shadow: 4px 4px 0px rgba(0,0,0,1);
}
.modal-btn-ok:hover { filter: brightness(1.1); transform: translate(1px,1px); box-shadow: none; }
</style>
