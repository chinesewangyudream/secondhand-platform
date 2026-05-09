package com.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotBlank(message = "举报原因不能为空")
    private String reason;
}
