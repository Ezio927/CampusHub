import { describe, it, expect, vi } from 'vitest'

import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'

vi.mock('@/components/layout/AppLayout.vue', () => ({
  default: { template: '<div><slot /></div>' },
}))

vi.mock('@/components/common/ModalContainer.vue', () => ({
  default: { template: '<div />' },
}))

import App from '../App.vue'

describe('App', () => {
  it('mounts renders properly', () => {
    localStorage.clear()
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia()],
        stubs: { RouterView: { template: '<main>router view</main>' } },
      },
    })
    expect(wrapper.text()).toContain('router view')
  })
})
