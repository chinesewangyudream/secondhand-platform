import request from './request'

export const chatApi = {
  getOrCreateSession: (data) => request.post('/chat/session', data),
  getSessions: () => request.get('/chat/sessions'),
  sendMessage: (sessionId, content, msgType = 0) =>
    request.post('/chat/send', { sessionId, content, msgType }),
  getMessages: (sessionId, page = 1) =>
    request.get(`/chat/messages/${sessionId}`, { params: { page } }),
  deleteMessage: (messageId) => request.delete(`/chat/message/${messageId}`),
  hideSession: (sessionId) => request.post(`/chat/session/${sessionId}/hide`)
}