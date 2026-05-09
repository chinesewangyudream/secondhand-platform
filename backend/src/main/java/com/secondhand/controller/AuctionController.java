package com.secondhand.controller;

import com.secondhand.dto.Result;
import com.secondhand.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /** 拍卖列表 */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(auctionService.getAuctionList(page, size));
    }

    /** 拍卖详情 */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(auctionService.getAuctionDetail(id));
    }

    /** 出价 */
    @PostMapping("/bid")
    public Result<?> bid(@RequestBody Map<String, Object> body) {
        Long auctionId = Long.parseLong(body.get("auctionId").toString());
        BigDecimal price = new BigDecimal(body.get("price").toString());
        auctionService.bid(auctionId, price);
        return Result.success("出价成功", null);
    }

    /** 出价历史 */
    @GetMapping("/bids/{auctionId}")
    public Result<?> bids(@PathVariable Long auctionId) {
        return Result.success(auctionService.getBidHistory(auctionId));
    }
}