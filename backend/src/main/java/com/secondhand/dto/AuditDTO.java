package com.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    // 审核结果：true-通过，false-驳回
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    // 违规原因（驳回时必填）
    private String violationReason;
}
