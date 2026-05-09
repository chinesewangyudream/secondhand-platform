package com.secondhand.controller;

import com.secondhand.dto.Result;
import com.secondhand.service.OrderService;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 从聊天会话创建订单（卖家发起）
     */
    @PostMapping("/create-from-session")
    public Result<?> createFromSession(@RequestBody Map<String, Object> body) {
        Long sessionId = Long.parseLong(body.get("sessionId").toString());
        Long goodsId = Long.parseLong(body.get("goodsId").toString());
        return Result.success(orderService.createOrderFromSession(sessionId, goodsId));
    }

    /**
     * 卖家改价
     */
    @PostMapping("/change-price/{orderId}")
    public Result<?> changePrice(@PathVariable Long orderId,
                                  @RequestBody Map<String, Object> body) {
        BigDecimal newPrice = new BigDecimal(body.get("newPrice").toString());
        return Result.success(orderService.changePrice(orderId, newPrice));
    }

    /**
     * 买家确认订单
     */
    @PostMapping("/confirm-order/{orderId}")
    public Result<?> confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return Result.success("订单已确认，请尽快付款", null);
    }

    /**
     * 买家拒绝订单
     */
    @PostMapping("/reject-order/{orderId}")
    public Result<?> rejectOrder(@PathVariable Long orderId) {
        orderService.rejectOrder(orderId);
        return Result.success("订单已取消", null);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result<?> getDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    /**
     * 根据会话获取订单
     */
    @GetMapping("/by-session/{sessionId}")
    public Result<?> getOrderBySession(@PathVariable Long sessionId) {
        return Result.success(orderService.getOrderBySession(sessionId));
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long goodsId = Long.parseLong(body.get("goodsId").toString());
        Long addressId = body.get("addressId") != null ?
                Long.parseLong(body.get("addressId").toString()) : null;
        String remark = (String) body.get("remark");
        return Result.success(orderService.createOrder(goodsId, addressId, remark));
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay/{orderNo}")
    public Result<?> pay(@PathVariable String orderNo) {
        orderService.payOrder(orderNo);
        return Result.success("支付成功", null);
    }

    /**
     * 发货
     */
    @PostMapping("/ship/{orderId}")
    public Result<?> ship(@PathVariable Long orderId,
                          @RequestBody Map<String, String> body) {
        orderService.shipOrder(orderId, body.get("company"), body.get("trackingNo"));
        return Result.success("发货成功", null);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm/{orderId}")
    public Result<?> confirm(@PathVariable Long orderId) {
        orderService.confirmReceive(orderId);
        return Result.success("确认收货成功", null);
    }

    /**
     * 查看物流
     */
    @GetMapping("/logistics/{orderId}")
    public Result<?> logistics(@PathVariable Long orderId) {
        return Result.success(orderService.getLogistics(orderId));
    }

    /**
     * 我的订单（买家）
     */
    @GetMapping("/my-buy")
    public Result<?> myBuyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getOrderListWithGoods(userId, true));
    }

    /**
     * 我的订单（卖家）
     */
    @GetMapping("/my-sell")
    public Result<?> mySellOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getOrderListWithGoods(userId, false));
    }
}
