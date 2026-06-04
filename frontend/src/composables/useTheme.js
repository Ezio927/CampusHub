import { ref } from 'vue'

const KEY = 'campushub-theme'
const DARK = 'dark'
const LIGHT = 'light'
const current = ref(load())

function load() {
  const v = localStorage.getItem(KEY) || LIGHT
  document.documentElement.setAttribute('data-theme', v)
  return v
}

export function useTheme() {
  function toggle() {
    current.value = current.value === DARK ? LIGHT : DARK
    localStorage.setItem(KEY, current.value)
    document.documentElement.setAttribute('data-theme', current.value)
  }
  const isDark = () => current.value === DARK
  return { toggle, isDark }
}
