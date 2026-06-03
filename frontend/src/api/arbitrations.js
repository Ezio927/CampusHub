import c from './client'

export const arbitrationAPI = {
  create: d => c.post('/arbitrations', d),
  mine: () => c.get('/arbitrations/mine'),
  pending: () => c.get('/arbitrations'),
  resolve: (id, d) => c.put(`/arbitrations/${id}/resolve`, d)
}

export const arbAPI = arbitrationAPI
