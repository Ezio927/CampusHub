import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { demandAPI } from '@/api/demands'

export const useDemandStore = defineStore('demands', () => {
  const list = ref([])
  const filters = reactive({ category:'', keyword:'', campus:'', sortBy:'createdAt', sortDir:'desc', openOnly: true })

  async function fetch() {
    try {
      const p = {}
      if (filters.category) p.category = filters.category
      if (filters.keyword) p.keyword = filters.keyword
      if (filters.campus) p.campus = filters.campus
      if (filters.openOnly) p.status = 'OPEN'
      if (filters.sortBy === 'recommended') {
        p.sortBy = 'recommended'
      } else {
        p.sortBy = filters.sortBy
        p.sortDir = filters.sortDir
      }
      const r = await demandAPI.search(p)
      list.value = r.data?.content || []
    } catch { list.value = [] }
  }

  return { list, filters, fetch }
})
