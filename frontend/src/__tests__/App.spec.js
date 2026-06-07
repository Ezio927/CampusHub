import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import App from '../App.vue'

describe('App', () => {
  it('mounts renders properly', () => {
    localStorage.clear()
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia()],
        stubs: {
          AppLayout: { template: '<div><slot /></div>' },
          RouterView: { template: '<main>router view</main>' },
        },
      },
    })
    expect(wrapper.text()).toContain('router view')
  })
})
