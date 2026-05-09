package com.secondhand.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    private String title;

    private String description;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格最低0.01")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @Min(1) @Max(10)
    private Integer conditionLevel;

    private List<String> images;
    private String location;
    private Integer isAuction;

    // 拍卖相关
    private BigDecimal startPrice;
    private BigDecimal minIncrement;
    private BigDecimal buyNowPrice;
    private String auctionStartTime;
    private String auctionEndTime;
}