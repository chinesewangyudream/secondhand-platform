package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reports")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;          // 被举报的商品ID
    private Long reporterId;       // 举报人ID
    private String reason;         // 举报原因
    private Integer status;        // 0待处理 1已处理-确认违规 2已处理-不违规
    private String handleResult;   // 处理结果说明
    private Long handlerId;        // 处理人ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
