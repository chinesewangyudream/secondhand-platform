package com.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.entity.*;
import com.secondhand.exception.BusinessException;
import com.secondhand.mapper.*;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final GoodsMapper goodsMapper;
    private final AddressMapper addressMapper;
    private final LogisticsMapper logisticsMapper;
    private final ChatSessionMapper chatSessionMapper;

    /**
     * 从聊天会话创建订单（卖家发起）
     */
    @Transactional
    public Map<String, Object> createOrderFromSession(Long sessionId, Long goodsId) {
        Long sellerId = SecurityUtils.getCurrentUserId();

        // 获取会话信息
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 验证是否是卖家
        if (!session.getSellerId().equals(sellerId)) {
            throw new BusinessException("只有卖家可以发起订单");
        }

        Long buyerId = session.getBuyerId();

        // 获取商品信息
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException("只能为自己的商品创建订单");
        }
        if (goods.getStatus() != 1) {
            throw new BusinessException("商品已下架或已售出");
        }

        // 检查是否已存在该商品待确认的订单
        Order existingOrder = lambdaQuery()
                .eq(Order::getGoodsId, goodsId)
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, 5)
                .one();
        if (existingOrder != null) {
            throw new BusinessException("该商品已存在待买家确认的订单");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setOriginalPrice(goods.getPrice());
        order.setAmount(goods.getPrice());
        order.setSessionId(sessionId);
        order.setPriceChanged(0);
        order.setStatus(5); // 待确认
        save(order);

        return buildOrderDetail(order, goods);
    }

    /**
     * 卖家改价
     */
    @Transactional
    public Map<String, Object> changePrice(Long orderId, java.math.BigDecimal newPrice) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException("只有卖家可以改价");
        }
        if (order.getStatus() != 5) {
            throw new BusinessException("只有待确认状态的订单可以改价");
        }
        if (newPrice == null || newPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("价格必须大于0");
        }

        order.setAmount(newPrice);
        order.setPriceChanged(1);
        updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        return buildOrderDetail(order, goods);
    }

    /**
     * 买家确认订单
     */
    @Transactional
    public void confirmOrder(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买家可以确认订单");
        }
        if (order.getStatus() != 5) {
            throw new BusinessException("订单状态不正确");
        }

        // 订单确认后变为待付款状态
        order.setStatus(0);
        updateById(order);

        // 商品下架
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null && goods.getStatus() == 1) {
            goods.setStatus(3); // 下架
            goodsMapper.updateById(goods);
        }
    }

    /**
     * 买家拒绝订单
     */
    @Transactional
    public void rejectOrder(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买家可以拒绝订单");
        }
        if (order.getStatus() != 5) {
            throw new BusinessException("订单状态不正确");
        }

        // 订单取消
        order.setStatus(4);
        updateById(order);
    }

    /**
     * 获取订单详情
     */
    public Map<String, Object> getOrderDetail(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        return buildOrderDetail(order, goods);
    }

    /**
     * 根据会话获取关联订单
     */
    public Map<String, Object> getOrderBySession(Long sessionId) {
        Order order = lambdaQuery()
                .eq(Order::getSessionId, sessionId)
                .in(Order::getStatus, 0, 1, 2, 3, 5)
                .orderByDesc(Order::getCreatedAt)
                .last("LIMIT 1")
                .one();

        if (order == null) {
            return null;
        }

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        return buildOrderDetail(order, goods);
    }

    /**
     * 构建订单详情
     */
    private Map<String, Object> buildOrderDetail(Order order, Goods goods) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("goodsId", order.getGoodsId());
        result.put("buyerId", order.getBuyerId());
        result.put("sellerId", order.getSellerId());
        result.put("originalPrice", order.getOriginalPrice());
        result.put("amount", order.getAmount());
        result.put("status", order.getStatus());
        result.put("priceChanged", order.getPriceChanged());
        result.put("createdAt", order.getCreatedAt());
        result.put("payTime", order.getPayTime());
        result.put("shipTime", order.getShipTime());
        result.put("confirmTime", order.getConfirmTime());

        if (goods != null) {
            Map<String, Object> goodsInfo = new HashMap<>();
            goodsInfo.put("id", goods.getId());
            goodsInfo.put("title", goods.getTitle());
            goodsInfo.put("price", goods.getPrice());
            goodsInfo.put("images", goods.getImages());
            goodsInfo.put("status", goods.getStatus());
            result.put("goods", goodsInfo);
        }

        return result;
    }

    /**
     * 创建订单
     */
    @Transactional
    public String createOrder(Long goodsId, Long addressId, String remark) {
        Long buyerId = SecurityUtils.getCurrentUserId();

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException("商品不存在或已售出");
        }
        if (goods.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setOriginalPrice(goods.getPrice());
        order.setAmount(goods.getPrice());
        order.setAddressId(addressId);
        order.setRemark(remark);
        order.setPriceChanged(0);
        order.setStatus(0); // 待付款
        save(order);

        // 锁定商品
        goods.setStatus(2); // 标记为已售
        goodsMapper.updateById(goods);

        return orderNo;
    }

    /**
     * 支付订单（模拟）
     */
    @Transactional
    public void payOrder(String orderNo) {
        Order order = lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("订单状态不正确");

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    /**
     * 发货（卖家操作）
     */
    @Transactional
    public void shipOrder(Long orderId, String company, String trackingNo) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        updateById(order);

        // 创建物流记录
        Logistics logistics = new Logistics();
        logistics.setOrderId(orderId);
        logistics.setCompany(company);
        logistics.setTrackingNo(trackingNo);
        logistics.setStatus("已发货");

        // 模拟物流轨迹
        List<Map<String, String>> details = new ArrayList<>();
        Map<String, String> node = new HashMap<>();
        node.put("time", LocalDateTime.now().toString());
        node.put("status", "卖家已发货，等待快递揽件");
        details.add(node);
        logistics.setDetails(com.alibaba.fastjson2.JSON.toJSONString(details));
        logisticsMapper.insert(logistics);
    }

    /**
     * 确认收货
     */
    @Transactional
    public void confirmReceive(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = getById(orderId);

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确");
        }

        order.setStatus(3);
        order.setConfirmTime(LocalDateTime.now());
        updateById(order);
    }

    /**
     * 查询物流
     */
    public Map<String, Object> getLogistics(Long orderId) {
        Order order = getById(orderId);
        Long userId = SecurityUtils.getCurrentUserId();

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        Logistics logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<Logistics>().eq(Logistics::getOrderId, orderId));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("logistics", logistics);
        if (logistics != null && logistics.getDetails() != null) {
            result.put("details", com.alibaba.fastjson2.JSON.parseArray(
                    logistics.getDetails()));
        }
        return result;
    }

    /**
     * 获取订单列表（包含商品信息）
     * @param userId 用户ID
     * @param isBuyer true=买家订单，false=卖家订单
     */
    public List<Map<String, Object>> getOrderListWithGoods(Long userId, boolean isBuyer) {
        List<Order> orders = lambdaQuery()
                .eq(isBuyer ? Order::getBuyerId : Order::getSellerId, userId)
                .orderByDesc(Order::getCreatedAt)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("goodsId", order.getGoodsId());
            item.put("buyerId", order.getBuyerId());
            item.put("sellerId", order.getSellerId());
            item.put("originalPrice", order.getOriginalPrice());
            item.put("amount", order.getAmount());
            item.put("status", order.getStatus());
            item.put("priceChanged", order.getPriceChanged());
            item.put("createdAt", order.getCreatedAt());
            item.put("payTime", order.getPayTime());
            item.put("shipTime", order.getShipTime());
            item.put("confirmTime", order.getConfirmTime());

            // 获取商品信息
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                item.put("goodsTitle", goods.getTitle());
                item.put("goodsImages", goods.getImages());
            }

            result.add(item);
        }
        return result;
    }

    private String generateOrderNo() {
        return "SH" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(9999));
    }
}