import request from './request'

export const auctionApi = {
  getList: (params) => request.get('/auction/list', { params }),
  getDetail: (id) => request.get(`/auction/detail/${id}`),
  bid: (auctionId, price) => request.post('/auction/bid', { auctionId, price }),
  getBidHistory: (auctionId) => request.get(`/auction/bids/${auctionId}`)
}