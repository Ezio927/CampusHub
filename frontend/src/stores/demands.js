import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { demandAPI } from '@/api/demands'

export const useDemandStore = defineStore('demands', () => {
  const demands = ref([])
  const filters = reactive({
    category: '',
    status: 'OPEN',
    keyword: '',
    locationTag: '',
    isUrgent: undefined,
    sortBy: 'createdAt',
    sortDir: 'desc',
  })

  async function fetchDemands() {
    const params = {}
    if (filters.category) params.category = filters.category
    if (filters.status) params.status = filters.status
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.locationTag) params.locationTag = filters.locationTag
    if (filters.isUrgent !== undefined) params.isUrgent = filters.isUrgent
    params.sortBy = filters.sortBy
    params.sortDir = filters.sortDir
    const res = await demandAPI.search(params)
    demands.value = res.data || []
  }

  return { demands, filters, fetchDemands }
})
