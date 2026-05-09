package com.secondhand.controller;

import com.secondhand.dto.AuditDTO;
import com.secondhand.dto.GoodsDTO;
import com.secondhand.dto.PageDTO;
import com.secondhand.dto.Result;
import com.secondhand.service.GoodsService;
import com.secondhand.service.AiPricingService;
import com.secondhand.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final AiPricingService aiPricingService;

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<?> publish(@Valid @RequestBody GoodsDTO dto) {
        Long goodsId = goodsService.publishGoods(dto);
        return Result.success("发布成功", goodsId);
    }

    /**
     * 商品列表
     */
    @GetMapping("/list")
    public Result<?> list(PageDTO dto) {
        return Result.success(goodsService.getGoodsList(dto));
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<?> search(PageDTO dto) {
        return Result.success(goodsService.getGoodsList(dto));
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(goodsService.getGoodsDetail(id));
    }

    /**
     * 我发布的商品
     */
    @GetMapping("/my")
    public Result<?> myGoods(@RequestParam(required = false) String status) {
        try {
            System.out.println("DEBUG /goods/my called, status: " + status);
            Long userId = SecurityUtils.getCurrentUserId();
            System.out.println("DEBUG Current user ID: " + userId);
            List<Map<String, Object>> result = goodsService.getMyGoods(userId, status);
            System.out.println("DEBUG /goods/my returning " + result.size() + " items");
            return Result.success(result);
        } catch (Exception e) {
            System.err.println("ERROR in /goods/my:");
            e.printStackTrace();
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 下架商品
     */
    @PutMapping("/off/{id}")
    public Result<?> offShelf(@PathVariable Long id) {
        com.secondhand.entity.Goods goods = goodsService.getById(id);
        if (!goods.getSellerId().equals(SecurityUtils.getCurrentUserId())) {
            return Result.error("无权限操作");
        }
        goods.setStatus(3);
        goodsService.updateById(goods);
        return Result.success("下架成功", null);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody GoodsDTO dto) {
        com.secondhand.entity.Goods goods = goodsService.getById(id);
        if (!goods.getSellerId().equals(SecurityUtils.getCurrentUserId())) {
            return Result.error("无权限操作");
        }
        goodsService.updateGoods(id, dto);
        return Result.success("更新成功", null);
    }

    /**
     * AI估价
     */
    @PostMapping("/ai-price")
    public Result<?> aiPrice(@RequestBody GoodsDTO dto) {
        java.math.BigDecimal price = aiPricingService.estimate(
                dto.getTitle(), dto.getDescription(), dto.getCategoryId(), dto.getConditionLevel());
        return Result.success("估价成功", price);
    }

    /**
     * AI生成描述和估价
     */
    @PostMapping("/ai-generate")
    public Result<?> aiGenerate(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        Integer categoryId = params.get("categoryId") != null ?
                Integer.parseInt(params.get("categoryId").toString()) : null;
        Integer condition = params.get("conditionLevel") != null ?
                Integer.parseInt(params.get("conditionLevel").toString()) : 7;
        return Result.success("生成成功", aiPricingService.generateDescription(title, categoryId, condition));
    }

    /**
     * AI商品推荐咨询
     */
    @PostMapping("/ai-recommend")
    public Result<?> aiRecommend(@RequestBody Map<String, Object> params) {
        String requirement = (String) params.get("requirement");
        java.math.BigDecimal minBudget = params.get("minBudget") != null ?
                new java.math.BigDecimal(params.get("minBudget").toString()) : null;
        java.math.BigDecimal maxBudget = params.get("maxBudget") != null ?
                new java.math.BigDecimal(params.get("maxBudget").toString()) : null;
        return Result.success("推荐成功", aiPricingService.recommendProducts(requirement, minBudget, maxBudget));
    }

    /**
     * 搜索推荐商品
     */
    @GetMapping("/recommend-search")
    public Result<?> recommendSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "6") int limit) {
        return Result.success(goodsService.searchRecommendedGoods(keyword, categoryId, minPrice, maxPrice, limit));
    }

    // ==================== 审核相关接口（审核员使用） ====================

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/audit/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> getPendingGoods(PageDTO dto) {
        return Result.success(goodsService.getPendingGoods(dto));
    }

    /**
     * 获取违规商品列表
     */
    @GetMapping("/audit/violation")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> getViolationGoods(PageDTO dto) {
        return Result.success(goodsService.getViolationGoods(dto));
    }

    /**
     * 审核商品
     */
    @PostMapping("/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> auditGoods(@Valid @RequestBody AuditDTO dto) {
        goodsService.auditGoods(dto);
        return Result.success(dto.getApproved() ? "审核通过" : "已驳回", null);
    }

    /**
     * 重新提交审核（卖家整改后使用）
     */
    @PostMapping("/resubmit/{id}")
    public Result<?> resubmitForAudit(@PathVariable Long id) {
        com.secondhand.entity.Goods goods = goodsService.getById(id);
        if (!goods.getSellerId().equals(SecurityUtils.getCurrentUserId())) {
            return Result.error("无权限操作");
        }
        goodsService.resubmitForAudit(id);
        return Result.success("已重新提交审核", null);
    }

    /**
     * 标记商品违规（审核员使用）
     */
    @PostMapping("/violation/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public Result<?> markAsViolation(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String violationReason = params.get("violationReason");
        goodsService.markAsViolation(id, violationReason);
        return Result.success("已标记为违规商品", null);
    }
}
