package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long goodsId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal originalPrice;  // 商品原价
    private BigDecimal amount;         // 实际交易价格（可能被改价）
    private Integer status;            // 0待付款 1待发货 2待收货 3已完成 4已取消 5待确认
    private Long addressId;
    private String remark;
    private Long sessionId;            // 关联的聊天会话ID
    private Integer priceChanged;      // 是否改价 0否 1是
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}