package com.secondhand.controller;

import com.secondhand.dto.PageDTO;
import com.secondhand.dto.ReportDTO;
import com.secondhand.dto.Result;
import com.secondhand.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 举报商品（普通用户使用）
     */
    @PostMapping("/submit")
    public Result<?> reportGoods(@Valid @RequestBody ReportDTO dto) {
        reportService.reportGoods(dto);
        return Result.success("举报成功，我们会尽快处理", null);
    }

    /**
     * 获取举报列表（审核员使用）
     * @param status 0待处理 1已处理-违规 2已处理-不违规，不传则查全部
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> getReportList(PageDTO dto, @RequestParam(required = false) Integer status) {
        return Result.success(reportService.getReportList(dto, status));
    }

    /**
     * 处理举报（审核员使用）
     */
    @PostMapping("/handle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> handleReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        boolean isViolation = Boolean.TRUE.equals(params.get("isViolation"));
        String handleResult = (String) params.get("handleResult");
        reportService.handleReport(id, isViolation, handleResult);
        return Result.success(isViolation ? "已确认违规并下架商品" : "已标记为不违规", null);
    }

    /**
     * 获取商品的举报次数
     */
    @GetMapping("/count/{goodsId}")
    public Result<?> getReportCount(@PathVariable Long goodsId) {
        return Result.success(reportService.getReportCount(goodsId));
    }
}
