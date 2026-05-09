package com.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService extends ServiceImpl<ChatSessionMapper, ChatSession> {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 获取或创建会话
     */
    @Transactional
    public ChatSession getOrCreateSession(Long goodsId, Long buyerId, Long sellerId) {
        ChatSession session = lambdaQuery()
                .eq(ChatSession::getGoodsId, goodsId)
                .eq(ChatSession::getBuyerId, buyerId)
                .eq(ChatSession::getSellerId, sellerId)
                .one();

        if (session == null) {
            session = new ChatSession();
            session.setGoodsId(goodsId);
            session.setBuyerId(buyerId);
            session.setSellerId(sellerId);
            session.setUnreadCountBuyer(0);
            session.setUnreadCountSeller(0);
            session.setHiddenByBuyer(false);
            session.setHiddenBySeller(false);
            save(session);
        } else {
            // 恢复被隐藏的会话
            if (buyerId.equals(session.getBuyerId()) && Boolean.TRUE.equals(session.getHiddenByBuyer())) {
                session.setHiddenByBuyer(false);
                updateById(session);
            } else if (sellerId.equals(session.getSellerId()) && Boolean.TRUE.equals(session.getHiddenBySeller())) {
                session.setHiddenBySeller(false);
                updateById(session);
            }
        }
        return session;
    }

    /**
     * 发送消息
     */
    @Transactional
    public ChatMessage sendMessage(Long sessionId, String content, Integer msgType) {
        Long senderId = SecurityUtils.getCurrentUserId();
        ChatSession session = getById(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");

        // 权限检查
        if (!session.getBuyerId().equals(senderId) &&
                !session.getSellerId().equals(senderId)) {
            throw new RuntimeException("无权限发送消息");
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : 0);
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        // 更新会话最后消息
        session.setLastMessage(content);
        session.setLastMessageTime(LocalDateTime.now());

        // 更新未读数
        if (senderId.equals(session.getBuyerId())) {
            session.setUnreadCountSeller(session.getUnreadCountSeller() + 1);
        } else {
            session.setUnreadCountBuyer(session.getUnreadCountBuyer() + 1);
        }
        updateById(session);

        // WebSocket推送给对方
        Long receiverId = senderId.equals(session.getBuyerId()) ?
                session.getSellerId() : session.getBuyerId();

        Map<String, Object> wsMsg = new HashMap<>();
        wsMsg.put("type", "NEW_MESSAGE");
        wsMsg.put("sessionId", sessionId);
        wsMsg.put("message", message);

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/chat",
                wsMsg
        );

        return message;
    }

    /**
     * 获取消息列表
     */
    public List<ChatMessage> getMessages(Long sessionId, Integer page) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatSession session = getById(sessionId);

        if (!session.getBuyerId().equals(userId) &&
                !session.getSellerId().equals(userId)) {
            throw new RuntimeException("无权限");
        }

        int offset = (page - 1) * 20;
        return chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreatedAt)
                        .last("LIMIT " + offset + ", 20")
        );
    }

    /**
     * 获取用户的会话列表
     */
    public List<Map<String, Object>> getSessionList() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatSession> sessions = lambdaQuery()
                .and(w -> w.eq(ChatSession::getBuyerId, userId)
                        .or().eq(ChatSession::getSellerId, userId))
                .orderByDesc(ChatSession::getLastMessageTime)
                .list();

        // 过滤掉被当前用户隐藏的会话
        sessions = sessions.stream()
                .filter(s -> {
                    if (userId.equals(s.getBuyerId())) {
                        return !Boolean.TRUE.equals(s.getHiddenByBuyer());
                    } else {
                        return !Boolean.TRUE.equals(s.getHiddenBySeller());
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", session.getId());
            item.put("goodsId", session.getGoodsId());
            item.put("buyerId", session.getBuyerId());
            item.put("sellerId", session.getSellerId());
            item.put("lastMessage", session.getLastMessage());
            item.put("lastMessageTime", session.getLastMessageTime());

            // 确定对方是谁
            Long partnerId = userId.equals(session.getBuyerId()) ?
                    session.getSellerId() : session.getBuyerId();
            item.put("partnerId", partnerId);

            // 获取对方用户信息
            User partner = userMapper.selectById(partnerId);
            if (partner != null) {
                item.put("partnerName", partner.getNickname() != null ?
                        partner.getNickname() : partner.getUsername());
                item.put("partnerAvatar", partner.getAvatar());
            }

            result.add(item);
        }
        return result;
    }

    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatMessage message = chatMessageMapper.selectById(messageId);

        if (message == null) {
            throw new RuntimeException("消息不存在");
        }

        // 只能删除自己发的消息
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("只能删除自己发送的消息");
        }

        chatMessageMapper.deleteById(messageId);

        // 如果是会话的最后一条消息，更新会话
        ChatSession session = getById(message.getSessionId());
        if (session != null && message.getContent().equals(session.getLastMessage())) {
            // 获取最新的消息作为最后消息
            ChatMessage lastMsg = chatMessageMapper.selectOne(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getSessionId, message.getSessionId())
                            .orderByDesc(ChatMessage::getCreatedAt)
                            .last("LIMIT 1")
            );
            if (lastMsg != null) {
                session.setLastMessage(lastMsg.getContent());
                session.setLastMessageTime(lastMsg.getCreatedAt());
            } else {
                session.setLastMessage(null);
                session.setLastMessageTime(null);
            }
            updateById(session);
        }
    }

    /**
     * 隐藏会话（结束聊天）
     */
    @Transactional
    public void hideSession(Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatSession session = getById(sessionId);

        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 权限检查
        if (!session.getBuyerId().equals(userId) &&
                !session.getSellerId().equals(userId)) {
            throw new RuntimeException("无权限");
        }

        // 根据当前用户身份设置隐藏状态
        if (userId.equals(session.getBuyerId())) {
            session.setHiddenByBuyer(true);
        } else {
            session.setHiddenBySeller(true);
        }
        updateById(session);
    }
}