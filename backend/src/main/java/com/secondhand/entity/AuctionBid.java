package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("auction_bids")
public class AuctionBid {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long auctionId;
    private Long userId;
    private BigDecimal bidPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
