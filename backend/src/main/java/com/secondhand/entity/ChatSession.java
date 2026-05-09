package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_sessions")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long buyerId;
    private Long sellerId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCountBuyer;
    private Integer unreadCountSeller;
    private Boolean hiddenByBuyer;
    private Boolean hiddenBySeller;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
