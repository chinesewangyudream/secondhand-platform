package com.secondhand.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.dto.AuditDTO;
import com.secondhand.dto.GoodsDTO;
import com.secondhand.dto.PageDTO;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> {

    private final AuctionMapper auctionMapper;
    private final GoodsMapper goodsMapper;
    private final AiPricingService aiPricingService;
    private final FavoriteMapper favoriteMapper;

    /**
     * 发布商品
     */
    @Transactional
    public Long publishGoods(GoodsDTO dto) {
        Long sellerId;
        try {
            sellerId = SecurityUtils.getCurrentUserId();
            System.out.println("DEBUG: Current user ID: " + sellerId);
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to get current user ID:");
            e.printStackTrace();
            throw e;
        }

        Goods goods = new Goods();
        goods.setTitle(dto.getTitle());
        goods.setDescription(dto.getDescription());
        goods.setCategoryId(dto.getCategoryId());
        goods.setSellerId(sellerId);
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setConditionLevel(dto.getConditionLevel());
        goods.setImages(JSON.toJSONString(dto.getImages()));
        goods.setLocation(dto.getLocation());
        goods.setViewCount(0);
        goods.setFavoriteCount(0);

        if (dto.getIsAuction() != null && dto.getIsAuction() == 1) {
            goods.setIsAuction(1);
            goods.setStatus(4); // 拍卖中
        } else {
            goods.setIsAuction(0);
            goods.setStatus(1); // 直接上架
        }

        // 异步AI估价
        save(goods);

        try {
            java.math.BigDecimal aiPrice = aiPricingService.estimate(
                    dto.getTitle(), dto.getDescription(), dto.getCategoryId(), dto.getConditionLevel());
            if (aiPrice != null) {
                goods.setAiEstimatedPrice(aiPrice);
                updateById(goods);
            }
        } catch (Exception e) {
            // AI估价失败不影响发布
            e.printStackTrace(); // 打印异常信息以便调试
        }

        // 如果是拍卖商品，创建拍卖记录
        if (dto.getIsAuction() != null && dto.getIsAuction() == 1) {
            Auction auction = new Auction();
            auction.setGoodsId(goods.getId());
            auction.setStartPrice(dto.getStartPrice());
            auction.setCurrentPrice(dto.getStartPrice());
            auction.setMinIncrement(dto.getMinIncrement() != null ?
                    dto.getMinIncrement() : java.math.BigDecimal.ONE);
            auction.setBuyNowPrice(dto.getBuyNowPrice());
            auction.setStartTime(LocalDateTime.parse(dto.getAuctionStartTime()
                    .replace(" ", "T")));
            auction.setEndTime(LocalDateTime.parse(dto.getAuctionEndTime()
                    .replace(" ", "T")));
            auction.setBidCount(0);
            auction.setStatus(0);
            auctionMapper.insert(auction);
        }

        return goods.getId();
    }

    /**
     * 搜索商品（多关键词 + 分类名称 + 地点 + 相关性排序）
     */
    public Page<Map<String, Object>> searchGoods(PageDTO dto) {
        List<String> keywords = new ArrayList<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            // 按空格拆分多个关键词
            for (String kw : dto.getKeyword().trim().split("\\s+")) {
                if (!kw.isEmpty()) {
                    keywords.add(kw);
                }
            }
        }

        long offset = (long) (dto.getPage() - 1) * dto.getSize();
        List<Map<String, Object>> records = goodsMapper.searchGoods(
                keywords.isEmpty() ? null : keywords,
                dto.getCategoryId(),
                dto.getMinPrice() != null ? java.math.BigDecimal.valueOf(dto.getMinPrice()) : null,
                dto.getMaxPrice() != null ? java.math.BigDecimal.valueOf(dto.getMaxPrice()) : null,
                dto.getConditionLevel(),
                offset,
                dto.getSize()
        );

        long total = goodsMapper.searchGoodsCount(
                keywords.isEmpty() ? null : keywords,
                dto.getCategoryId(),
                dto.getMinPrice() != null ? java.math.BigDecimal.valueOf(dto.getMinPrice()) : null,
                dto.getMaxPrice() != null ? java.math.BigDecimal.valueOf(dto.getMaxPrice()) : null,
                dto.getConditionLevel()
        );

        // 排序处理：如果有排序需求，在内存中重新排序
        if (dto.getSortBy() != null && !keywords.isEmpty()) {
            // 有搜索关键词时默认按相关性排序，但可以按价格重新排序
            if ("price_asc".equals(dto.getSortBy())) {
                records.sort((a, b) -> {
                    java.math.BigDecimal pa = (java.math.BigDecimal) a.get("price");
                    java.math.BigDecimal pb = (java.math.BigDecimal) b.get("price");
                    return pa.compareTo(pb);
                });
            } else if ("price_desc".equals(dto.getSortBy())) {
                records.sort((a, b) -> {
                    java.math.BigDecimal pa = (java.math.BigDecimal) a.get("price");
                    java.math.BigDecimal pb = (java.math.BigDecimal) b.get("price");
                    return pb.compareTo(pa);
                });
            }
        } else if (dto.getSortBy() != null && keywords.isEmpty()) {
            // 无关键词时按指定排序
            if ("price_asc".equals(dto.getSortBy())) {
                records.sort((a, b) -> {
                    java.math.BigDecimal pa = (java.math.BigDecimal) a.get("price");
                    java.math.BigDecimal pb = (java.math.BigDecimal) b.get("price");
                    return pa.compareTo(pb);
                });
            } else if ("price_desc".equals(dto.getSortBy())) {
                records.sort((a, b) -> {
                    java.math.BigDecimal pa = (java.math.BigDecimal) a.get("price");
                    java.math.BigDecimal pb = (java.math.BigDecimal) b.get("price");
                    return pb.compareTo(pa);
                });
            }
        }

        // 解析图片JSON
        for (Map<String, Object> map : records) {
            parseImages(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(dto.getPage(), dto.getSize(), total);
        resultPage.setRecords(records);
        return resultPage;
    }

    private void parseImages(Map<String, Object> map) {
        Object imagesObj = map.get("images");
        List<String> images = new ArrayList<>();
        if (imagesObj != null) {
            String imagesStr = imagesObj.toString();
            if (!imagesStr.isEmpty()) {
                try {
                    images = JSON.parseArray(imagesStr, String.class);
                } catch (Exception e) {
                    images.add(imagesStr);
                }
            }
        }
        map.put("images", images);
    }

    /**
     * 分页查询商品
     */
    public Page<Map<String, Object>> getGoodsList(PageDTO dto) {
        Page<Goods> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1); // 只查在售

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Goods::getTitle, dto.getKeyword())
                    .or().like(Goods::getDescription, dto.getKeyword()));
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, dto.getCategoryId());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(Goods::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Goods::getPrice, dto.getMaxPrice());
        }

        // 排序
        if ("price_asc".equals(dto.getSortBy())) {
            wrapper.orderByAsc(Goods::getPrice);
        } else if ("price_desc".equals(dto.getSortBy())) {
            wrapper.orderByDesc(Goods::getPrice);
        } else if ("popular".equals(dto.getSortBy())) {
            wrapper.orderByDesc(Goods::getViewCount);
        } else {
            wrapper.orderByDesc(Goods::getCreatedAt);
        }

        page(page, wrapper);

        // 转换为Map，解析图片
        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(),
                page.getSize(), page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Goods g : page.getRecords()) {
            records.add(convertGoodsToMap(g));
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 商品详情
     */
    public Map<String, Object> getGoodsDetail(Long id) {
        Goods goods = getById(id);
        if (goods == null) throw new RuntimeException("商品不存在");

        // 增加浏览量
        goods.setViewCount(goods.getViewCount() + 1);
        updateById(goods);

        Map<String, Object> detail = convertGoodsToMap(goods);

        // 如果是拍卖商品，附带拍卖信息
        if (goods.getIsAuction() == 1) {
            Auction auction = auctionMapper.selectOne(
                    new LambdaQueryWrapper<Auction>().eq(Auction::getGoodsId, id));
            if (auction != null) {
                detail.put("auction", auction);
            }
        }

        return detail;
    }

    private Map<String, Object> convertGoodsToMap(Goods g) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", g.getId());
        map.put("title", g.getTitle());
        map.put("description", g.getDescription());
        map.put("categoryId", g.getCategoryId());
        map.put("sellerId", g.getSellerId());
        map.put("price", g.getPrice());
        map.put("originalPrice", g.getOriginalPrice());
        map.put("conditionLevel", g.getConditionLevel());
        // 解析图片JSON，处理异常情况
        List<String> images = new ArrayList<>();
        if (g.getImages() != null && !g.getImages().isEmpty()) {
            try {
                images = JSON.parseArray(g.getImages(), String.class);
            } catch (Exception e) {
                // JSON解析失败，尝试作为单个URL处理
                images.add(g.getImages());
            }
        }
        map.put("images", images);
        map.put("location", g.getLocation());
        map.put("status", g.getStatus());
        map.put("violationReason", g.getViolationReason());
        map.put("viewCount", g.getViewCount());
        map.put("favoriteCount", g.getFavoriteCount());
        map.put("isAuction", g.getIsAuction());
        map.put("aiEstimatedPrice", g.getAiEstimatedPrice());
        map.put("createdAt", g.getCreatedAt());
        return map;
    }

    /**
     * 获取当前用户发布的商品（转换后的Map）
     */
    public List<Map<String, Object>> getMyGoods(Long userId, String status) {
        try {
            System.out.println("DEBUG getMyGoods: userId=" + userId + ", status=" + status);

            LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Goods::getSellerId, userId);

            // 根据状态过滤
            if (status != null && !status.isEmpty()) {
                switch (status) {
                    case "selling":
                        // 在售或拍卖中
                        wrapper.and(w -> w.eq(Goods::getStatus, 1).or().eq(Goods::getStatus, 4));
                        break;
                    case "sold":
                        wrapper.eq(Goods::getStatus, 2);
                        break;
                    case "off":
                        wrapper.eq(Goods::getStatus, 3);
                        break;
                    case "pending":
                        wrapper.eq(Goods::getStatus, 0);
                        break;
                    case "violation":
                        // 违规和违规-待审核状态
                        wrapper.and(w -> w.eq(Goods::getStatus, 5).or().eq(Goods::getStatus, 6));
                        break;
                    // 默认不添加状态过滤
                }
            }

            wrapper.orderByDesc(Goods::getCreatedAt);
            List<Goods> goodsList = this.list(wrapper);
            System.out.println("DEBUG getMyGoods: found " + goodsList.size() + " goods");

            List<Map<String, Object>> result = new ArrayList<>();
            for (Goods g : goodsList) {
                result.add(convertGoodsToMap(g));
            }
            return result;
        } catch (Exception e) {
            System.err.println("ERROR in getMyGoods:");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 更新商品
     */
    @Transactional
    public void updateGoods(Long id, GoodsDTO dto) {
        Goods goods = getById(id);
        if (goods == null) throw new RuntimeException("商品不存在");

        goods.setTitle(dto.getTitle());
        goods.setDescription(dto.getDescription());
        goods.setCategoryId(dto.getCategoryId());
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setConditionLevel(dto.getConditionLevel());
        goods.setImages(JSON.toJSONString(dto.getImages()));
        goods.setLocation(dto.getLocation());

        updateById(goods);
    }

    /**
     * 获取待审核商品列表（审核员使用）
     */
    public Page<Map<String, Object>> getPendingGoods(PageDTO dto) {
        Page<Goods> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 0); // 待审核
        wrapper.orderByDesc(Goods::getCreatedAt);

        page(page, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(),
                page.getSize(), page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Goods g : page.getRecords()) {
            records.add(convertGoodsToMap(g));
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 审核商品（审核员使用）
     */
    @Transactional
    public void auditGoods(AuditDTO dto) {
        Goods goods = getById(dto.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 支持待审核(0)和违规-待审核(6)两种状态
        if (goods.getStatus() != 0 && goods.getStatus() != 6) {
            throw new RuntimeException("该商品不是待审核状态");
        }

        if (dto.getApproved()) {
            // 审核通过
            goods.setStatus(1);
            goods.setViolationReason(null);
        } else {
            // 审核驳回，标记为违规
            if (!StringUtils.hasText(dto.getViolationReason())) {
                throw new RuntimeException("驳回时必须填写违规原因");
            }
            goods.setStatus(5); // 违规状态
            goods.setViolationReason(dto.getViolationReason());
        }

        updateById(goods);
    }

    /**
     * 获取违规商品列表（审核员使用）- 轻量版
     */
    public Page<Map<String, Object>> getViolationGoods(PageDTO dto) {
        Page<Goods> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        // 查询违规(5)和违规-待审核(6)状态
        wrapper.in(Goods::getStatus, 5, 6);
        wrapper.orderByDesc(Goods::getCreatedAt);

        // 只查询需要的字段，减少数据传输
        wrapper.select(Goods::getId, Goods::getTitle, Goods::getPrice,
                Goods::getOriginalPrice, Goods::getConditionLevel, Goods::getImages,
                Goods::getLocation, Goods::getStatus, Goods::getViolationReason,
                Goods::getViewCount, Goods::getCreatedAt, Goods::getUpdatedAt);

        page(page, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(),
                page.getSize(), page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Goods g : page.getRecords()) {
            records.add(convertGoodsToMapLight(g));
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 轻量级商品转换（用于列表展示，减少字段）
     */
    private Map<String, Object> convertGoodsToMapLight(Goods g) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", g.getId());
        map.put("title", g.getTitle());
        map.put("price", g.getPrice());
        map.put("originalPrice", g.getOriginalPrice());
        map.put("conditionLevel", g.getConditionLevel());
        // 只解析第一张图片用于列表展示
        List<String> images = new ArrayList<>();
        if (g.getImages() != null && !g.getImages().isEmpty()) {
            try {
                List<String> allImages = JSON.parseArray(g.getImages(), String.class);
                if (allImages != null && !allImages.isEmpty()) {
                    images.add(allImages.get(0));
                }
            } catch (Exception e) {
                images.add(g.getImages());
            }
        }
        map.put("images", images);
        map.put("location", g.getLocation());
        map.put("status", g.getStatus());
        map.put("violationReason", g.getViolationReason());
        map.put("viewCount", g.getViewCount());
        map.put("createdAt", g.getCreatedAt());
        map.put("updatedAt", g.getUpdatedAt());
        return map;
    }

    /**
     * 重新提交审核（卖家整改后使用）
     */
    @Transactional
    public void resubmitForAudit(Long goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        if (goods.getStatus() != 5) {
            throw new RuntimeException("只有违规商品才能重新提交审核");
        }

        // 改为违规-待审核状态，商品仍在违规列表中但显示"已提交申请"
        goods.setStatus(6);
        updateById(goods);
    }

    /**
     * 标记商品违规（审核员使用）
     */
    @Transactional
    public void markAsViolation(Long goodsId, String violationReason) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 只有在售商品才能标记违规
        if (goods.getStatus() != 1 && goods.getStatus() != 4) {
            throw new RuntimeException("只有在售或拍卖中的商品才能标记违规");
        }

        if (!StringUtils.hasText(violationReason)) {
            throw new RuntimeException("必须填写违规原因");
        }

        goods.setStatus(5); // 违规状态
        goods.setViolationReason(violationReason);
        updateById(goods);
    }

    /**
     * 根据关键词和价格范围搜索推荐商品
     */
    public List<Map<String, Object>> searchRecommendedGoods(String keyword, Integer categoryId,
                                                             java.math.BigDecimal minPrice,
                                                             java.math.BigDecimal maxPrice,
                                                             int limit) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1); // 只查在售

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Goods::getTitle, keyword)
                    .or().like(Goods::getDescription, keyword));
        }

        // 分类过滤
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }

        // 价格范围
        if (minPrice != null) {
            wrapper.ge(Goods::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Goods::getPrice, maxPrice);
        }

        wrapper.orderByDesc(Goods::getViewCount);
        wrapper.last("LIMIT " + limit);

        List<Goods> goodsList = list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Goods g : goodsList) {
            result.add(convertGoodsToMap(g));
        }
        return result;
    }
}