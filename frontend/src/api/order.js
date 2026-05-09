import request from './request'

export const orderApi = {
  // 从聊天会话创建订单（卖家发起）
  createFromSession: (sessionId, goodsId) =>
    request.post('/order/create-from-session', { sessionId, goodsId }),

  // 卖家改价
  changePrice: (orderId, newPrice) =>
    request.post(`/order/change-price/${orderId}`, { newPrice }),

  // 买家确认订单
  confirmOrder: (orderId) =>
    request.post(`/order/confirm-order/${orderId}`),

  // 买家拒绝订单
  rejectOrder: (orderId) =>
    request.post(`/order/reject-order/${orderId}`),

  // 获取订单详情
  getDetail: (orderId) =>
    request.get(`/order/detail/${orderId}`),

  // 根据会话获取订单
  getOrderBySession: (sessionId) =>
    request.get(`/order/by-session/${sessionId}`),

  create: (data) => request.post('/order/create', data),
  pay: (orderNo) => request.post(`/order/pay/${orderNo}`),
  ship: (orderId, data) => request.post(`/order/ship/${orderId}`, data),
  confirm: (orderId) => request.post(`/order/confirm/${orderId}`),
  getLogistics: (orderId) => request.get(`/order/logistics/${orderId}`),
  getMyBuyOrders: () => request.get('/order/my-buy'),
  getMySellOrders: () => request.get('/order/my-sell')
}