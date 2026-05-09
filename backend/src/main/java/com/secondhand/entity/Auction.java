package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("auctions")
public class Auction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;
    private BigDecimal buyNowPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long winnerId;
    private Integer bidCount;
    private Integer status;    // 0未开始 1进行中 2已结束

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}