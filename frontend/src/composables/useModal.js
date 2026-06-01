import { reactive } from 'vue'

const state = reactive({
  show: false,
  type: 'alert',
  title: '',
  message: '',
  inputValue: '',
  inputPlaceholder: '',
  resolve: null
})

function alert(title, message = '') {
  return new Promise(resolve => {
    state.type = 'alert'
    state.title = title
    state.message = message
    state.resolve = resolve
    state.show = true
  })
}

function confirm(title, message = '') {
  return new Promise(resolve => {
    state.type = 'confirm'
    state.title = title
    state.message = message
    state.resolve = resolve
    state.show = true
  })
}

function prompt(title, message = '', placeholder = '') {
  return new Promise(resolve => {
    state.type = 'prompt'
    state.title = title
    state.message = message
    state.inputValue = ''
    state.inputPlaceholder = placeholder
    state.resolve = resolve
    state.show = true
  })
}

function close(result) {
  state.show = false
  if (state.resolve) {
    state.resolve(result)
    state.resolve = null
  }
}

export function useModal() {
  return { state, alert, confirm, prompt, close }
}
