import request from './request'

export const goodsApi = {
  getList: (params) => request.get('/goods/list', { params }),
  search: (params) => request.get('/goods/search', { params }),
  getDetail: (id) => request.get(`/goods/detail/${id}`),
  publish: (data) => request.post('/goods/publish', data),
  update: (id, data) => request.put(`/goods/update/${id}`, data),
  getMyGoods: (params) => request.get('/goods/my', { params }),
  offShelf: (id) => request.put(`/goods/off/${id}`),
  getCategories: () => request.get('/category/list'),
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  aiGenerate: (params) => request.post('/goods/ai-generate', params),

  // AI推荐咨询
  aiRecommend: (params) => request.post('/goods/ai-recommend', params),
  recommendSearch: (params) => request.get('/goods/recommend-search', { params }),

  // 收藏相关
  addFavorite: (goodsId) => request.post(`/favorite/add/${goodsId}`),
  removeFavorite: (goodsId) => request.delete(`/favorite/remove/${goodsId}`),
  checkFavorite: (goodsId) => request.get(`/favorite/check/${goodsId}`),
  getMyFavorites: (params) => request.get('/favorite/my', { params }),

  // 审核相关
  getPendingGoods: (params) => request.get('/goods/audit/pending', { params }),
  getViolationGoods: (params) => request.get('/goods/audit/violation', { params }),
  auditGoods: (data) => request.post('/goods/audit', data),
  resubmitForAudit: (id) => request.post(`/goods/resubmit/${id}`),
  markAsViolation: (id, violationReason) => request.post(`/goods/violation/${id}`, { violationReason })
}