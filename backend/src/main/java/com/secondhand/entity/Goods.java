package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Integer categoryId;
    private Long sellerId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer conditionLevel;
    private String images;        // JSON字符串
    private String location;
    private Integer status;       // 0待审核 1在售 2已售 3下架 4拍卖中 5违规 6违规-待审核
    private String violationReason; // 违规原因
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer isAuction;
    private BigDecimal aiEstimatedPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}