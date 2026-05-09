import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref([])
  const currentSession = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)
  let stompClient = null

  function connectWebSocket(userId) {
    const token = localStorage.getItem('token')
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        console.log('WebSocket 已连接')
        // 订阅个人消息队列
        stompClient.subscribe(`/user/${userId}/queue/chat`, (msg) => {
          const data = JSON.parse(msg.body)
          handleNewMessage(data)
        })
      },
      onDisconnect: () => console.log('WebSocket 已断开'),
      reconnectDelay: 5000
    })
    stompClient.activate()
  }

  function handleNewMessage(data) {
    if (data.type === 'NEW_MESSAGE') {
      // 如果是当前会话的消息，直接添加
      if (currentSession.value?.id === data.sessionId) {
        messages.value.push(data.message)
      }
      unreadCount.value++
    }
  }

  function disconnect() {
    if (stompClient) stompClient.deactivate()
  }

  return { 
    sessions, currentSession, messages, 
    unreadCount, connectWebSocket, disconnect 
  }
})