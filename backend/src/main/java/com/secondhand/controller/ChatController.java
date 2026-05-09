package com.secondhand.controller;

import com.secondhand.dto.Result;
import com.secondhand.service.ChatService;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** 获取/创建会话 */
    @PostMapping("/session")
    public Result<?> getOrCreateSession(@RequestBody Map<String, Long> body) {
        Long goodsId = body.get("goodsId");
        Long sellerId = body.get("sellerId");
        Long buyerId = SecurityUtils.getCurrentUserId();
        return Result.success(chatService.getOrCreateSession(goodsId, buyerId, sellerId));
    }

    /** 会话列表 */
    @GetMapping("/sessions")
    public Result<?> sessions() {
        return Result.success(chatService.getSessionList());
    }

    /** 发送消息（HTTP方式） */
    @PostMapping("/send")
    public Result<?> sendMessage(@RequestBody Map<String, Object> body) {
        Long sessionId = Long.parseLong(body.get("sessionId").toString());
        String content = (String) body.get("content");
        Integer msgType = body.get("msgType") != null ?
                Integer.parseInt(body.get("msgType").toString()) : 0;
        return Result.success(chatService.sendMessage(sessionId, content, msgType));
    }

    /** 获取消息历史 */
    @GetMapping("/messages/{sessionId}")
    public Result<?> messages(@PathVariable Long sessionId,
                              @RequestParam(defaultValue = "1") int page) {
        return Result.success(chatService.getMessages(sessionId, page));
    }

    /** 删除消息 */
    @DeleteMapping("/message/{messageId}")
    public Result<?> deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
        return Result.success("删除成功", null);
    }

    /** 隐藏会话（结束聊天） */
    @PostMapping("/session/{sessionId}/hide")
    public Result<?> hideSession(@PathVariable Long sessionId) {
        chatService.hideSession(sessionId);
        return Result.success("已结束聊天", null);
    }
}