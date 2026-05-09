import request from './request'

export const reportApi = {
  // 举报商品
  submit: (data) => request.post('/report/submit', data),

  // 获取举报列表（审核员使用）
  getList: (params) => request.get('/report/list', { params }),

  // 处理举报（审核员使用）
  handle: (id, data) => request.post(`/report/handle/${id}`, data),

  // 获取商品的举报次数
  getCount: (goodsId) => request.get(`/report/count/${goodsId}`)
}
