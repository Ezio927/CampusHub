import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { demandAPI } from '@/api/demands'

export const useDemandStore = defineStore('demands', () => {
  const list = ref([])
  const filters = reactive({ category:'', keyword:'', sortBy:'createdAt', sortDir:'desc' })

  async function fetch() {
    const p = {}
    if (filters.category) p.category = filters.category
    if (filters.keyword) p.keyword = filters.keyword
    p.sortBy = filters.sortBy
    p.sortDir = filters.sortDir
    const r = await demandAPI.search(p)
    list.value = r.data || []
  }

  return { list, filters, fetch }
})
