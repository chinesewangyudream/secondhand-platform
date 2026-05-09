import request from './request'

export const userApi = {
  getUserInfo: (id) => request.get(`/user/${id}/info`)
}
