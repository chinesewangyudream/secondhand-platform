package com.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.entity.*;
import com.secondhand.exception.BusinessException;
import com.secondhand.mapper.*;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuctionService extends ServiceImpl<AuctionMapper, Auction> {

    private final AuctionBidMapper auctionBidMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    /**
     * 出价
     */
    @Transactional
    public void bid(Long auctionId, BigDecimal bidPrice) {
        Long userId = SecurityUtils.getCurrentUserId();
        Auction auction = getById(auctionId);

        if (auction == null) throw new BusinessException("拍卖不存在");

        LocalDateTime now = LocalDateTime.now();
        // 更新拍卖状态
        if (now.isBefore(auction.getStartTime())) {
            throw new BusinessException("拍卖尚未开始");
        }
        if (now.isAfter(auction.getEndTime())) {
            throw new BusinessException("拍卖已结束");
        }

        auction.setStatus(1); // 进行中

        // 验证出价
        BigDecimal minBid = auction.getCurrentPrice().add(auction.getMinIncrement());
        if (bidPrice.compareTo(minBid) < 0) {
            throw new BusinessException("出价必须大于等于 " + minBid);
        }

        // 检查是否是商品卖家
        Goods goods = goodsMapper.selectById(auction.getGoodsId());
        if (goods.getSellerId().equals(userId)) {
            throw new BusinessException("不能对自己的商品出价");
        }

        // 更新拍卖
        auction.setCurrentPrice(bidPrice);
        auction.setBidCount(auction.getBidCount() + 1);
        auction.setWinnerId(userId);
        updateById(auction);

        // 记录出价
        AuctionBid bid = new AuctionBid();
        bid.setAuctionId(auctionId);
        bid.setUserId(userId);
        bid.setBidPrice(bidPrice);
        auctionBidMapper.insert(bid);

        // 一口价购买
        if (auction.getBuyNowPrice() != null &&
                bidPrice.compareTo(auction.getBuyNowPrice()) >= 0) {
            endAuction(auctionId);
        }
    }

    /**
     * 结束拍卖
     */
    @Transactional
    public void endAuction(Long auctionId) {
        Auction auction = getById(auctionId);
        auction.setStatus(2);
        updateById(auction);

        if (auction.getWinnerId() != null) {
            Goods goods = goodsMapper.selectById(auction.getGoodsId());
            goods.setStatus(2); // 已售
            goodsMapper.updateById(goods);

            // 创建拍卖订单
            createAuctionOrder(auction, goods);
        }
    }

    /**
     * 拍卖成交后创建订单
     */
    private void createAuctionOrder(Auction auction, Goods goods) {
        // 检查是否已存在订单
        Order existingOrder = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getGoodsId, goods.getId())
                        .eq(Order::getBuyerId, auction.getWinnerId())
                        .in(Order::getStatus, 0, 1, 2, 3, 5)
        );
        if (existingOrder != null) {
            return; // 已存在订单，不重复创建
        }

        String orderNo = "AU" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(9999));

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setGoodsId(goods.getId());
        order.setBuyerId(auction.getWinnerId());
        order.setSellerId(goods.getSellerId());
        order.setOriginalPrice(auction.getCurrentPrice());
        order.setAmount(auction.getCurrentPrice());
        order.setPriceChanged(0);
        order.setStatus(0); // 待付款
        orderMapper.insert(order);
    }

    /**
     * 获取进行中的拍卖列表
     */
    public Page<Map<String, Object>> getAuctionList(int page, int size) {
        Page<Auction> auctionPage = new Page<>(page, size);

        // 自动开始未开始但已到时间的拍卖
        lambdaUpdate()
                .le(Auction::getStartTime, LocalDateTime.now())
                .ge(Auction::getEndTime, LocalDateTime.now())
                .eq(Auction::getStatus, 0)
                .set(Auction::getStatus, 1)
                .update();

        // 自动结束已过期的拍卖，并为成交的拍卖创建订单
        List<Auction> endedAuctions = lambdaQuery()
                .lt(Auction::getEndTime, LocalDateTime.now())
                .ne(Auction::getStatus, 2)
                .list();

        for (Auction auction : endedAuctions) {
            auction.setStatus(2);
            updateById(auction);

            if (auction.getWinnerId() != null) {
                Goods goods = goodsMapper.selectById(auction.getGoodsId());
                if (goods != null) {
                    goods.setStatus(2); // 已售
                    goodsMapper.updateById(goods);
                    // 创建拍卖订单
                    createAuctionOrder(auction, goods);
                }
            }
        }

        page(auctionPage, new LambdaQueryWrapper<Auction>()
                .in(Auction::getStatus, 0, 1)
                .orderByAsc(Auction::getEndTime));

        Page<Map<String, Object>> resultPage = new Page<>(page, size, auctionPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        for (Auction a : auctionPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("auction", a);
            Goods goods = goodsMapper.selectById(a.getGoodsId());
            map.put("goods", goods);
            records.add(map);
        }
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 获取出价历史
     */
    public List<Map<String, Object>> getBidHistory(Long auctionId) {
        List<AuctionBid> bids = auctionBidMapper.selectList(
                new LambdaQueryWrapper<AuctionBid>()
                        .eq(AuctionBid::getAuctionId, auctionId)
                        .orderByDesc(AuctionBid::getCreatedAt)
                        .last("LIMIT 20")
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AuctionBid bid : bids) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bid.getId());
            item.put("auctionId", bid.getAuctionId());
            item.put("userId", bid.getUserId());
            item.put("bidPrice", bid.getBidPrice());
            item.put("createdAt", bid.getCreatedAt());

            // 获取用户信息
            User user = userMapper.selectById(bid.getUserId());
            if (user != null) {
                item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
            }

            result.add(item);
        }
        return result;
    }

    /**
     * 获取拍卖详情（包含商品信息）
     */
    public Map<String, Object> getAuctionDetail(Long auctionId) {
        Auction auction = getById(auctionId);
        if (auction == null) {
            throw new BusinessException("拍卖不存在");
        }

        // 检查拍卖是否应该结束
        if (auction.getStatus() != 2 && LocalDateTime.now().isAfter(auction.getEndTime())) {
            // 拍卖到期，自动结束
            auction.setStatus(2);
            updateById(auction);

            if (auction.getWinnerId() != null) {
                Goods goods = goodsMapper.selectById(auction.getGoodsId());
                if (goods != null) {
                    goods.setStatus(2); // 已售
                    goodsMapper.updateById(goods);
                    // 创建拍卖订单
                    createAuctionOrder(auction, goods);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", auction.getId());
        result.put("goodsId", auction.getGoodsId());
        result.put("startPrice", auction.getStartPrice());
        result.put("currentPrice", auction.getCurrentPrice());
        result.put("minIncrement", auction.getMinIncrement());
        result.put("buyNowPrice", auction.getBuyNowPrice());
        result.put("bidCount", auction.getBidCount());
        result.put("startTime", auction.getStartTime());
        result.put("endTime", auction.getEndTime());
        result.put("status", auction.getStatus());
        result.put("winnerId", auction.getWinnerId());

        // 获取商品信息
        Goods goods = goodsMapper.selectById(auction.getGoodsId());
        if (goods != null) {
            result.put("title", goods.getTitle());
            result.put("description", goods.getDescription());
            result.put("images", goods.getImages());
            result.put("sellerId", goods.getSellerId());
            result.put("location", goods.getLocation());
        }

        return result;
    }
}